import React, { useState, useEffect, use } from 'react';
import { useAuth } from '../AuthContext';
import { todoListAPI, todoItemAPI, authAPI } from '../api';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPen, faCheck, faLink, faTrash, faCross, faX, faCalendar } from '@fortawesome/free-solid-svg-icons';
import Swal from 'sweetalert2'
import { TodoItemStatus, getStatusColor } from '../constants/TodoItemStatus';
import './TodoApp.css';
import { text } from '@fortawesome/fontawesome-svg-core';

function TodoApp() {
  const { user, logout, updateUserInfo } = useAuth();
  const [todoLists, setTodoLists] = useState([]);
  const [selectedList, setSelectedList] = useState(null);
  const [todoItems, setTodoItems] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // List form state
  const [showListForm, setShowListForm] = useState(false);
  const [listName, setListName] = useState('');
  const [editingListId, setEditingListId] = useState(null);

  // User info pop up state
  const [showUserInfo, setShowUserInfo] = useState(false);

  useEffect(() => {
    if (showUserInfo) {
      Swal.fire({
        title: 'Your Info',
        html: `
          <input id="swal-input-username" class="swal2-input" placeholder="Username" value="${user?.username}">
          <input id="swal-input-email" class="swal2-input" placeholder="Email" value="${user?.email}">
          <input id="swal-input-password" type="password" class="swal2-input" placeholder="Enter password to confirm changes">
        `,
        footer: `
          <button id="delete-account-btn" class="swal-delete-account-btn">
            <span>&#9888; Delete Account</span>
          </button>
        `,
        customClass: {
          popup: 'custom-swal-popup',
          htmlContainer: 'custom-swal-html',
          footer: 'custom-swal-footer'
        },
        focusConfirm: false,
        showConfirmButton: true,
        confirmButtonText: 'Update',
        showCancelButton: true,
        cancelButtonText: 'Close',
        didOpen: () => {
          const deleteBtn = document.getElementById('delete-account-btn');
          deleteBtn.addEventListener('click', async () => {
            Swal.close();
            const confirmDelete = await Swal.fire({
              title: 'Delete Account?',
              html: `
                <p style="margin-bottom: 15px; color: #666;">This action cannot be undone. All your lists and items will be permanently deleted.</p>
                <input id="delete-password" type="password" class="swal2-input" placeholder="Enter your password to confirm">
              `,
              icon: 'warning',
              showCancelButton: true,
              confirmButtonText: 'Yes, delete my account',
              confirmButtonColor: '#f44336',
              cancelButtonText: 'Cancel',
              preConfirm: () => {
                const password = document.getElementById('delete-password').value;
                if (!password) {
                  Swal.showValidationMessage('Password is required');
                  return false;
                }
                return password;
              }
            });
            
            if (confirmDelete.isConfirmed) {
              try {
                await authAPI.deleteAccount({ password: confirmDelete.value });
                localStorage.clear();
                Swal.fire({
                  icon: 'success',
                  title: 'Account Deleted',
                  text: 'Your account has been permanently deleted.',
                  timer: 2000,
                  showConfirmButton: false
                }).then(() => {
                  window.location.reload();
                });
              } catch (error) {
                Swal.fire({
                  icon: 'error',
                  title: 'Delete Failed',
                  text: error.response?.data?.message || 'Failed to delete account.'
                });
                setShowUserInfo(true);
              }
            } else {
              setShowUserInfo(true);
            }
          });
        },
        preConfirm: () => {
          const username = document.getElementById('swal-input-username').value;
          const email = document.getElementById('swal-input-email').value;
          const password = document.getElementById('swal-input-password').value;

          if (!password) {
            Swal.showValidationMessage('Password is required to update info');
            return false;
          }

          return { username, email, password };
        }
      }).then(async (result) => {
        if (result.isConfirmed) {
          try {
            await updateUserInfo(result.value);
            Swal.fire({
              icon: 'success',
              title: 'Info Updated',
              text: 'Your user information has been updated successfully.',
              timer: 1500,
              showConfirmButton: false,
              position: 'top'
            });
          } catch (error) {
            Swal.fire({
              icon: 'error',
              title: 'Update Failed',
              text: error.response?.data?.message || 'Failed to update user information.',
            });
          }
        }
        
        setShowUserInfo(false);
      });
    }
  }, [showUserInfo]);
  
  // Item form state
  const [showItemForm, setShowItemForm] = useState(false);
  const [itemForm, setItemForm] = useState({
    name: '',
    description: '',
    deadline: '',
    status: TodoItemStatus.NOT_STARTED
  });
  const [editingItemId, setEditingItemId] = useState(null);
  
  // Dependency management - handled by SweetAlert
  
  // Filters and sorting
  const [filters, setFilters] = useState({
    status: '',
    expired: '',
    name: ''
  });
  const [sorting, setSorting] = useState({
    sortBy: 'deadline',
    sortOrder: 'asc'
  });

  useEffect(() => {
    fetchTodoLists();
  }, []);

  useEffect(() => {
    if (selectedList) {
      fetchTodoItems();
    }
  }, [selectedList, filters, sorting]);

  useEffect(() => {
    if (!showItemForm) {
      setItemForm({ name: '', description: '', deadline: '', status: TodoItemStatus.NOT_STARTED });
      setEditingItemId(null);
    } else {
      Swal.fire({
        title: editingItemId ? 'Edit Todo Item' : 'Add New Todo Item',
        html: `
        <input id="swal-input-name" class="swal2-input" placeholder="Item name" value="${itemForm.name}" maxlength="50" required>
        <textarea id="swal-input-description" class="swal2-textarea" placeholder="Description" maxlength="150" rows="3">${itemForm.description}</textarea>
        <input id="swal-input-deadline" class="swal2-input swal2-date-input" type="date" value="${itemForm.deadline}" required>
        <select id="swal-input-status" class="swal2-select">
          <option value="${TodoItemStatus.NOT_STARTED}" ${itemForm.status === TodoItemStatus.NOT_STARTED ? 'selected' : ''}>Not Started</option>
          <option value="${TodoItemStatus.IN_PROGRESS}" ${itemForm.status === TodoItemStatus.IN_PROGRESS ? 'selected' : ''}>In Progress</option>
          <option value="${TodoItemStatus.COMPLETED}" ${itemForm.status === TodoItemStatus.COMPLETED ? 'selected' : ''}>Completed</option>
        </select>
      `,
        width: '450px',
        padding: '20px',
        showCancelButton: true,
        cancelButtonText: 'Cancel',
        confirmButtonText: 'Save',
        focusConfirm: false,
        customClass: {
          popup: 'custom-swal-popup',
          htmlContainer: 'custom-swal-html'
        },
        preConfirm: () => {
          const name = document.getElementById('swal-input-name').value;
          const description = document.getElementById('swal-input-description').value;
          const deadline = document.getElementById('swal-input-deadline').value;
          const status = document.getElementById('swal-input-status').value;

          if (!name.trim()) {
            Swal.showValidationMessage('Item name is required');
            return false;
          }

          if (!deadline) {
            Swal.showValidationMessage('Deadline is required');
            return false;
          }

          if (name.length > 50) {
            Swal.showValidationMessage('Name cannot exceed 50 characters');
            return false;
          }

          if (description.length > 150) {
            Swal.showValidationMessage('Description cannot exceed 150 characters');
            return false;
          }

          return { name, description, deadline, status };
        }
      }).then(async (result) => {
        if (result.isConfirmed) {
          const formData = result.value;
          
          try {
            const data = {
              name: formData.name,
              description: formData.description,
              deadline: formData.deadline || null,
              status: formData.status
            };

            if (editingItemId) {
              await todoItemAPI.update(selectedList.id, editingItemId, data);
            } else {
              await todoItemAPI.create(selectedList.id, data);
            }

            setItemForm({ name: '', description: '', deadline: '', status: TodoItemStatus.NOT_STARTED });
            setShowItemForm(false);
            setEditingItemId(null);
            fetchTodoItems();
            
            Swal.fire({
              icon: 'success',
              title: 'Success!',
              text: editingItemId ? 'Item updated successfully' : 'Item created successfully',
              timer: 600,
              showConfirmButton: false,
              position: 'top'
            });
          } catch (error) {
            setShowItemForm(false);
            setEditingItemId(null);
            
            error.response?.data?.message
              ? Swal.fire({
                  icon: 'warning',
                  title: 'Dependency Warning',
                  text: error.response.data.message
                })
              : Swal.fire({
                  icon: 'error',
                  title: 'Error',
                  text: 'Backend error occurred while creating/updating the item.'
                });
          }
        } else {
          setShowItemForm(false);
          setEditingItemId(null);
        }
      });
    }

  }, [showItemForm]);

  useEffect(() => {

    if (showListForm) {
      Swal.fire({
        title: editingListId ? 'Edit Todo List' : 'Create New Todo List',
        input: 'text',
        customClass: {
          popup: 'custom-swal-popup',
          input: 'custom-swal-input'
        },
        inputPlaceholder: 'List name',
        inputValue: listName,
        inputAttributes: {
          maxlength: 20,
          autocapitalize: 'off',
          autocorrect: 'off'
        },
        showCancelButton: true,
        cancelButtonText: 'Cancel',
        confirmButtonText: 'Save',
        preConfirm: (name) => {
          if (!name.trim()) {
            Swal.showValidationMessage('List name is required');
            return false;
          }
          if (name.length > 20) {
            Swal.showValidationMessage('List name cannot exceed 20 characters');
            return false;
          }
          return name;
        }
      }).then(async (result) => {
        if (result.isConfirmed) {
          const name = result.value;
          try {
            if (editingListId) {
              await todoListAPI.update(editingListId, { name });
            } else {
              const response = await todoListAPI.create({ name });
              setSelectedList(response.data);
            }
            setListName('');
            setShowListForm(false);
            setEditingListId(null);
            fetchTodoLists();
            Swal.fire({
              icon: 'success',
              title: 'Success!',
              text: editingListId ? 'List updated successfully' : 'List created successfully',
              timer: 600,
              showConfirmButton: false,
              position: 'top'
            });
          }
          catch (error) {
            error.response?.data?.message
              ? Swal.fire({
                  icon: 'warning',
                  title: 'Warning',
                  text: error.response.data.message
                })
              : Swal.fire({
                  icon: 'error',
                  title: 'Error',
                  text: 'Backend error occurred while creating/updating the list.'
                });
          }
        } else {
          setShowListForm(false);
          setEditingListId(null);
        }
      });
    }

  }, [showListForm]);

  const fetchTodoLists = async () => {
    try {
      setLoading(true);
      const response = await todoListAPI.getAll();
      setTodoLists(response.data);
    } catch (error) {
      if (error.response?.status === 401) {
        Swal.fire({
          icon: 'error',
          title: 'Session Expired',
          text: 'Please login again.'
        });
        logout();
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchTodoItems = async () => {
    if (!selectedList) return;
    
    try {
      const params = {
        ...filters,
        ...sorting
      };
      // Remove empty filters
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null) {
          delete params[key];
        }
      });
      
      const response = await todoItemAPI.getAll(selectedList.id, params);
      setTodoItems(response.data); // Backend handles expired status
    } catch (error) {
      Swal.fire({
        icon: 'error',
        title: 'Error',
        text: 'Backend error occurred while fetching todo items.'
      });
    }
  };

  const handleDeleteList = async (listId) => {

    Swal.fire({
      title: 'Are you sure?',
      text: 'This will delete the entire list and all its items.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'Cancel'
    }).then(async (result) => {
      if (result.isConfirmed) {
        try {
          await todoListAPI.delete(listId);
          if (selectedList?.id === listId) {
            setSelectedList(null);
            setTodoItems([]);
          }
          setSelectedList(null);
          fetchTodoLists();
        } catch (error) {
          Swal.fire({
            icon: 'error',
            title: 'Error',
            text: error.response?.data?.message || 'Backend error occurred while deleting the list.'
          });
        }
      }
    });
  };

  const handleEditItem = (item) => {
    const previousDeadline = item.deadline ? item.deadline.substring(0, 10) : '';
    setItemForm({
      name: item.name,
      description: item.description || '',
      deadline: previousDeadline,
      status: item.status
    });
    setEditingItemId(item.id);
    setShowItemForm(true);
  };

  const handleMarkComplete = async (itemId) => {
    try {
      await todoItemAPI.markComplete(selectedList.id, itemId);
      fetchTodoItems();
    } catch (error) {
      error.response?.data?.message
        ? Swal.fire({
            icon: 'warning',
            title: 'Dependency Warning',
            text: error.response.data.message
          })
        : Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Backend error occurred while marking the item as complete.'
          });
    }
  };

  const handleDeleteItem = async (itemId) => {
    Swal.fire({
      title: 'Are you sure?',
      text: 'This will delete the todo item.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'Cancel'
    }).then(async (result) => {
      if (result.isConfirmed) {
        try {
          await todoItemAPI.delete(selectedList.id, itemId);
          fetchTodoItems();
        } catch (error) {
          error.response?.data?.message
            ? Swal.fire({
                icon: 'warning',
                title: 'Warning',
                text: error.response.data.message
              })
            : Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Backend error occurred while deleting the item.'
              });
        }
      }
    });
  };

  const handleAddDependency = async (itemId, dependencyId) => {
    try {
      await todoItemAPI.addDependency(selectedList.id, itemId, dependencyId);
      fetchTodoItems();
      Swal.fire({
        icon: 'success',
        title: 'Success!',
        text: 'Dependency added successfully',
        timer: 600,
        showConfirmButton: false,
        position: 'top'
      });
    } catch (error) {
      error.response?.data?.message
        ? Swal.fire({
            icon: 'warning',
            title: 'Warning',
            text: error.response.data.message
          })
        : Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Failed to add dependency'
          });
    }
  };

  const handleRemoveDependency = async (itemId, dependencyId) => {
    try {
      await todoItemAPI.removeDependency(selectedList.id, itemId, dependencyId);
      fetchTodoItems();
    } catch (error) {
      Swal.fire({
        icon: 'error',
        title: 'Error',
        text: 'Backend error occurred while removing the dependency.'
      });
    }
  };

  const showDependencySelection = (item) => {
    const availableItems = todoItems.filter(
      i => i.id !== item.id && !item.dependencies?.some(d => d.id === i.id)
    );

    if (availableItems.length === 0) {
      Swal.fire({
        icon: 'info',
        title: 'No Available Items',
        text: 'There are no items available to add as a dependency.',
        confirmButtonText: 'OK'
      });
      return;
    }

    const optionsHtml = availableItems.map(i => `
      <div class="swal-dependency-option" data-id="${i.id}">
        <span class="dep-name">${i.name}</span>
        <span class="dep-status-badge" style="background-color: ${getStatusColor(i.status)};">
          ${i.status.replace('_', ' ')}
        </span>
      </div>
    `).join('');

    Swal.fire({
      title: `Add Dependency to: ${item.name}`,
      html: `
        <p style="margin-bottom: 15px; color: #666;">Select an item that must be completed first:</p>
        <div class="swal-dependency-list">
          ${optionsHtml}
        </div>
      `,
      width: '500px',
      showCancelButton: true,
      showConfirmButton: false,
      cancelButtonText: 'Close',
      customClass: {
        popup: 'custom-swal-popup',
        htmlContainer: 'custom-swal-html'
      },
      didOpen: () => {
        const options = document.querySelectorAll('.swal-dependency-option');
        options.forEach(option => {
          option.addEventListener('click', async () => {
            const depId = parseInt(option.getAttribute('data-id'));
            Swal.close();
            await handleAddDependency(item.id, depId);
          });
        });
      }
    });
  };

  const isExpired = (item) => {
    if (!item.deadline || item.status === TodoItemStatus.COMPLETED) return false;
    const deadlineDate = new Date(item.deadline + 'T00:00:00');
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return deadlineDate < today;
  };

  const isLastDay = (item) => {
    if (!item.deadline || item.status === TodoItemStatus.COMPLETED) return false;
    const deadlineDate = new Date(item.deadline + 'T00:00:00');
    const today = new Date();
    deadlineDate.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);
    return deadlineDate.getTime() === today.getTime();
  };

  return (
    <div className="todo-app">
      <header className="app-header">
        <h1>Todo Lists</h1>
        <div className="user-info">
          <button onClick={() => setShowUserInfo(true)} className="btn-user-info"><span>Welcome, {user?.username}!</span></button>
          <button onClick={logout} className="btn-logout">Logout</button>
        </div>
      </header>

      <div className="app-content">
        {/* Lists Sidebar */}
        <div className="lists-sidebar">
          <div className="sidebar-header">
            <h2>My Lists</h2>
            <button onClick={() => { setShowListForm(true); setEditingListId(null); setListName(''); }} className="btn-add">
              + New List
            </button>
          </div>

          <div className="lists-container">
            {todoLists.map(list => (
              <div
                key={list.id}
                className={`list-item ${selectedList?.id === list.id ? 'active' : ''}`}
                onClick={() => setSelectedList(list)}
              >
                <span className="list-name">{list.name}</span>
                <div className="list-actions">
                  <button onClick={(e) => { e.stopPropagation(); setListName(list.name); setEditingListId(list.id); setShowListForm(true); }} className="btn-icon">Edit</button>
                  <button onClick={(e) => { e.stopPropagation(); handleDeleteList(list.id); }} className="btn-icon">Delete</button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Items Section */}
        <div className="items-section">
          {selectedList ? (
            <>
              <div className="items-header">
                <h2>{selectedList.name}</h2>
                <button onClick={() => { setShowItemForm(true); setEditingItemId(null); setItemForm({ name: '', description: '', deadline: '', status: TodoItemStatus.NOT_STARTED }); }} className="btn-add">
                  + Add Item
                </button>
              </div>

              {/* Filters and Sorting */}
              <div className="filters-bar">
                <select value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
                  <option value="">All Status</option>
                  <option value={TodoItemStatus.NOT_STARTED}>Not Started</option>
                  <option value={TodoItemStatus.IN_PROGRESS}>In Progress</option>
                  <option value={TodoItemStatus.COMPLETED}>Completed</option>
                  <option value={TodoItemStatus.EXPIRED}>Expired</option>
                </select>

                <input
                  type="text"
                  placeholder="Search by name..."
                  value={filters.name}
                  onChange={(e) => setFilters({ ...filters, name: e.target.value })}
                  className="search-input"
                />

                <select value={sorting.sortBy} onChange={(e) => setSorting({ ...sorting, sortBy: e.target.value })}>
                  <option value="deadline">Sort: Deadline</option>
                  <option value="createdate">Sort: Create Date</option>
                  <option value="name">Sort: Name</option>
                  <option value="status">Sort: Status</option>
                </select>

                <button onClick={() => setSorting({ ...sorting, sortOrder: sorting.sortOrder === 'asc' ? 'desc' : 'asc' })} className="btn-sort">
                  {sorting.sortOrder === 'asc' ? <p>&#129035;</p> : <p>&#129033;</p>}
                </button>
              </div>

              

              {/* Items List */}
              <div className="items-list">
                {todoItems.length === 0 ? (
                  <p className="empty-state">No items yet. Add one above!</p>
                ) : (
                  todoItems.map(item => (
                    <div key={item.id} className={`item-card ${isExpired(item) ? 'expired' : ''} ${item.status === 'COMPLETED' ? 'completed' : ''}`}>
                      <div className="item-main">
                        <div className="item-header">
                          <h3>{item.name}</h3>
                          <span className="status-badge" style={{ backgroundColor: getStatusColor(item.status) }}>
                            {item.status.replace('_', ' ')}
                          </span>
                          
                          <div className="item-actions">
                            {item.status !== 'COMPLETED' && (
                              <button onClick={() => handleMarkComplete(item.id)} className="btn-complete" title="Mark as complete">
                                <FontAwesomeIcon icon={faCheck} />
                              </button>
                            )}
                            <button onClick={() => handleEditItem(item)} className="btn-edit" title="Edit">
                              <FontAwesomeIcon icon={faPen} />
                            </button>
                            <button onClick={() => showDependencySelection(item)} className="btn-dependency" title="Add dependency">
                              <FontAwesomeIcon icon={faLink} />
                            </button>
                            <button onClick={() => handleDeleteItem(item.id)} className="btn-delete" title="Delete">
                              <FontAwesomeIcon icon={faTrash} />
                            </button>
                          </div>
                        </div>
                        
                        {item.description && <p className="item-description">{item.description}</p>}
                        
                        <div className="item-meta">
                          {item.deadline && (
                            <span className={`deadline ${item.status === 'COMPLETED' ? 'completed' : ''} ${isExpired(item) ? 'expired' : ''}`}>
                              <FontAwesomeIcon icon={faCalendar} /> Deadline: {new Date(item.deadline).toLocaleDateString()}
                            </span>
                          )}
                          <span className="created">Created: {new Date(item.createdAt).toLocaleDateString()}</span>
                          {isLastDay(item) && !isExpired(item) && (<span className="last-day-badge">Last Day!</span>)}
                        </div>

                        {item.dependencies && item.dependencies.length > 0 && (
                          <div className="dependencies">
                            <strong>Depends on:</strong>
                            {item.dependencies.map(dep => (
                              <span key={dep.id} className="dependency-tag">
                                {dep.name}
                                <button onClick={() => handleRemoveDependency(item.id, dep.id)} className="btn-remove-dep">&#10006;</button>
                              </span>
                            ))}
                          </div>
                        )}
                      </div>

                    </div>
                  ))
                )}
              </div>
            </>
          ) : (
            <div className="empty-state-main">
              <h2>Select a list or create a new one</h2>
            </div>
          )}
        </div>
      </div>

    </div>
  );
}

export default TodoApp;
