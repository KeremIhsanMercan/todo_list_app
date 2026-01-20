import React, { useState, useEffect } from 'react';
import { useAuth } from '../AuthContext';
import { todoListAPI, todoItemAPI } from '../api';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPen, faCheck, faLink, faTrash, faCross, faX, faCalendar } from '@fortawesome/free-solid-svg-icons';
import './TodoApp.css';

function TodoApp() {
  const { user, logout } = useAuth();
  const [todoLists, setTodoLists] = useState([]);
  const [selectedList, setSelectedList] = useState(null);
  const [todoItems, setTodoItems] = useState([]);
  const [loading, setLoading] = useState(false);
  
  // List form state
  const [showListForm, setShowListForm] = useState(false);
  const [listName, setListName] = useState('');
  const [editingListId, setEditingListId] = useState(null);
  
  // Item form state
  const [showItemForm, setShowItemForm] = useState(false);
  const [itemForm, setItemForm] = useState({
    name: '',
    description: '',
    deadline: '',
    status: 'NOT_STARTED'
  });
  const [editingItemId, setEditingItemId] = useState(null);
  
  // Dependency management
  const [showDependencyModal, setShowDependencyModal] = useState(false);
  const [selectedItemForDep, setSelectedItemForDep] = useState(null);
  
  // Filters and sorting
  const [filters, setFilters] = useState({
    status: '',
    expired: '',
    name: ''
  });
  const [sorting, setSorting] = useState({
    sortBy: 'createdate',
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

  const fetchTodoLists = async () => {
    try {
      setLoading(true);
      const response = await todoListAPI.getAll();
      setTodoLists(response.data);
      if (response.data.length > 0 && !selectedList) {
        setSelectedList(response.data[0]);
      }
    } catch (error) {
      console.error('Error fetching todo lists:', error);
      if (error.response?.status === 401) {
        alert('Session expired. Please login again.');
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
      setTodoItems(response.data);
    } catch (error) {
      console.error('Error fetching todo items:', error);
    }
  };

  // List operations
  const handleCreateList = async (e) => {
    e.preventDefault();
    if (!listName.trim()) return;

    try {
      if (editingListId) {
        await todoListAPI.update(editingListId, { name: listName });
      } else {
        const response = await todoListAPI.create({ name: listName });
        setSelectedList(response.data);
      }
      setListName('');
      setShowListForm(false);
      setEditingListId(null);
      fetchTodoLists();
    } catch (error) {
      alert('Failed to save list: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleDeleteList = async (listId) => {
    if (!window.confirm('Delete this list and all its items?')) return;

    try {
      await todoListAPI.delete(listId);
      if (selectedList?.id === listId) {
        setSelectedList(null);
        setTodoItems([]);
      }
      fetchTodoLists();
    } catch (error) {
      alert('Failed to delete list');
    }
  };

  // Item operations
  const handleItemFormSubmit = async (e) => {
    e.preventDefault();
    if (!selectedList) return;

    if (!itemForm.name.trim()) {
      alert('Item name is required');
      return;
    }

    if (itemForm.deadline) {
      const deadlineDate = new Date(itemForm.deadline);
      if (isNaN(deadlineDate.getTime())) {
        alert('Invalid deadline date');
        return;
      }
    }

    if (itemForm.description.length > 100) {
      alert('Description cannot exceed 100 characters');
      return;
    }

    if (itemForm.name.length > 50) {
      alert('Name cannot exceed 50 characters');
      return;
    }

    try {
      const data = {
        ...itemForm,
        deadline: itemForm.deadline ? itemForm.deadline + 'T23:59:59' : null
      };

      if (editingItemId) {
        await todoItemAPI.update(selectedList.id, editingItemId, data);
      } else {
        await todoItemAPI.create(selectedList.id, data);
      }

      setItemForm({ name: '', description: '', deadline: '', status: 'NOT_STARTED' });
      setShowItemForm(false);
      setEditingItemId(null);
      fetchTodoItems();
    } catch (error) {
      alert('Failed to save item: ' + (error.response?.data?.message || error.message));
    }
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
      alert(error.response?.data?.message || 'Failed to mark as complete');
    }
  };

  const handleDeleteItem = async (itemId) => {
    if (!window.confirm('Delete this item?')) return;

    
    try {
      await todoItemAPI.delete(selectedList.id, itemId);
      fetchTodoItems();
    } catch (error) {
      if (error.response?.data?.message) {
        alert(error.response.data.message);
      } else {
        alert('Failed to delete item');
      }
    }
  };

  const handleAddDependency = async (dependencyId) => {
    if (!selectedItemForDep) return;

    try {
      await todoItemAPI.addDependency(selectedList.id, selectedItemForDep.id, dependencyId);
      fetchTodoItems();
      setShowDependencyModal(false);
      setSelectedItemForDep(null);
    } catch (error) {
      alert(error.response?.data?.message || 'Failed to add dependency');
    }
  };

  const handleRemoveDependency = async (itemId, dependencyId) => {
    try {
      await todoItemAPI.removeDependency(selectedList.id, itemId, dependencyId);
      fetchTodoItems();
    } catch (error) {
      alert('Failed to remove dependency');
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED': return '#4caf50';
      case 'IN_PROGRESS': return '#ff9800';
      default: return '#9e9e9e';
    }
  };

  const isExpired = (item) => {
    if (!item.deadline || item.status === 'COMPLETED') return false;
    return new Date(item.deadline) < new Date();
  };

  return (
    <div className="todo-app">
      <header className="app-header">
        <h1>Todo Lists</h1>
        <div className="user-info">
          <span>Welcome, {user?.username}!</span>
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

          {showListForm && (
            <form onSubmit={handleCreateList} className="list-form">
              <input
                type="text"
                placeholder="List name"
                value={listName}
                onChange={(e) => setListName(e.target.value)}
                required
                maxLength="100"
                autoFocus
              />
              <div className="form-buttons">
                <button type="submit" className="btn-primary">Save</button>
                <button type="button" onClick={() => { setShowListForm(false); setEditingListId(null); }} className="btn-secondary">Cancel</button>
              </div>
            </form>
          )}

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
                <button onClick={() => { setShowItemForm(true); setEditingItemId(null); setItemForm({ name: '', description: '', deadline: '', status: 'NOT_STARTED' }); }} className="btn-add">
                  + Add Item
                </button>
              </div>

              {/* Filters and Sorting */}
              <div className="filters-bar">
                <select value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}>
                  <option value="">All Status</option>
                  <option value="NOT_STARTED">Not Started</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                </select>

                <select value={filters.expired} onChange={(e) => setFilters({ ...filters, expired: e.target.value })}>
                  <option value="">All Items</option>
                  <option value="true">Expired</option>
                  <option value="false">Not Expired</option>
                </select>

                <input
                  type="text"
                  placeholder="Search by name..."
                  value={filters.name}
                  onChange={(e) => setFilters({ ...filters, name: e.target.value })}
                  className="search-input"
                />

                <select value={sorting.sortBy} onChange={(e) => setSorting({ ...sorting, sortBy: e.target.value })}>
                  <option value="createdate">Sort: Create Date</option>
                  <option value="deadline">Sort: Deadline</option>
                  <option value="name">Sort: Name</option>
                  <option value="status">Sort: Status</option>
                </select>

                <button onClick={() => setSorting({ ...sorting, sortOrder: sorting.sortOrder === 'asc' ? 'desc' : 'asc' })} className="btn-sort">
                  {sorting.sortOrder === 'asc' ? <p>&#129035;</p> : <p>&#129033;</p>}
                </button>
              </div>

              {/* Item Form */}
              {showItemForm && (
                <form onSubmit={handleItemFormSubmit} className="item-form">
                  <input
                    type="text"
                    placeholder="Item name"
                    value={itemForm.name}
                    onChange={(e) => setItemForm({ ...itemForm, name: e.target.value })}
                    required
                    maxLength="200"
                  />
                  <textarea
                    placeholder="Description"
                    value={itemForm.description}
                    onChange={(e) => setItemForm({ ...itemForm, description: e.target.value })}
                    maxLength="1000"
                    rows="3"
                  />
                  <input
                    type="date"
                    value={itemForm.deadline}
                    onChange={(e) => setItemForm({ ...itemForm, deadline: e.target.value })}
                  />
                  <select value={itemForm.status} onChange={(e) => setItemForm({ ...itemForm, status: e.target.value })}>
                    <option value="NOT_STARTED">Not Started</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="COMPLETED">Completed</option>
                  </select>
                  <div className="form-buttons">
                    <button type="submit" className="btn-primary">{editingItemId ? 'Update' : 'Add'} Item</button>
                    <button type="button" onClick={() => { setShowItemForm(false); setEditingItemId(null); }} className="btn-secondary">Cancel</button>
                  </div>
                </form>
              )}

              {/* Items List */}
              <div className="items-list">
                {todoItems.length === 0 ? (
                  <p className="empty-state">No items yet. Add one above!</p>
                ) : (
                  todoItems.map(item => (
                    <div key={item.id} className={`item-card ${isExpired(item) ? 'expired' : ''}`}>
                      <div className="item-main">
                        <div className="item-header">
                          <h3>{item.name}</h3>
                          <span className="status-badge" style={{ backgroundColor: getStatusColor(item.status) }}>
                            {item.status.replace('_', ' ')}
                          </span>
                        </div>
                        
                        {item.description && <p className="item-description">{item.description}</p>}
                        
                        <div className="item-meta">
                          {item.deadline && (
                            <span className={isExpired(item) ? 'deadline expired' : 'deadline'}>
                              <FontAwesomeIcon icon={faCalendar} /> Deadline: {new Date(item.deadline).toLocaleString()}
                            </span>
                          )}
                          <span className="created">Created: {new Date(item.createdAt).toLocaleDateString()}</span>
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

                      <div className="item-actions">
                        {item.status !== 'COMPLETED' && (
                          <button onClick={() => handleMarkComplete(item.id)} className="btn-complete" title="Mark as complete">
                            <FontAwesomeIcon icon={faCheck} />
                          </button>
                        )}
                        <button onClick={() => handleEditItem(item)} className="btn-edit" title="Edit">
                          <FontAwesomeIcon icon={faPen} />
                        </button>
                        <button onClick={() => { setSelectedItemForDep(item); setShowDependencyModal(true); }} className="btn-dependency" title="Add dependency">
                          <FontAwesomeIcon icon={faLink} />
                        </button>
                        <button onClick={() => handleDeleteItem(item.id)} className="btn-delete" title="Delete">
                          <FontAwesomeIcon icon={faTrash} />
                        </button>
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

      {/* Dependency Modal */}
      {showDependencyModal && selectedItemForDep && (
        <div className="modal-overlay" onClick={() => setShowDependencyModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Add Dependency to: {selectedItemForDep.name}</h3>
            <p>Select an item that must be completed first:</p>
            <div className="dependency-list">
              {todoItems.filter(item => item.id !== selectedItemForDep.id && !selectedItemForDep.dependencies?.some(d => d.id === item.id)).map(item => (
                <div key={item.id} className="dependency-option" onClick={() => handleAddDependency(item.id)}>
                  <span>{item.name}</span>
                  <span className="status-badge" style={{ backgroundColor: getStatusColor(item.status) }}>
                    {item.status.replace('_', ' ')}
                  </span>
                </div>
              ))}
            </div>
            <button onClick={() => { setShowDependencyModal(false); setSelectedItemForDep(null); }} className="btn-secondary">Close</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default TodoApp;
