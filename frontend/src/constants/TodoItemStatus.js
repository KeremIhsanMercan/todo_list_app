// TodoItem Status Enum
export const TodoItemStatus = {
  NOT_STARTED: 'NOT_STARTED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  EXPIRED: 'EXPIRED'
};

// Helper function to get status display name
export const getStatusDisplayName = (status) => {
  switch (status) {
    case TodoItemStatus.NOT_STARTED:
      return 'Not Started';
    case TodoItemStatus.IN_PROGRESS:
      return 'In Progress';
    case TodoItemStatus.COMPLETED:
      return 'Completed';
    case TodoItemStatus.EXPIRED:
      return 'Expired';
    default:
      return status;
  }
};

// Helper function to get status color
export const getStatusColor = (status) => {
  switch (status) {
    case TodoItemStatus.COMPLETED:
      return '#4caf50';
    case TodoItemStatus.IN_PROGRESS:
      return '#ff9800';
    case TodoItemStatus.EXPIRED:
      return '#f44336';
    case TodoItemStatus.NOT_STARTED:
    default:
      return '#9e9e9e';
  }
};

export default TodoItemStatus;
