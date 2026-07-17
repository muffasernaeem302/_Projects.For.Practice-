const todoInput = document.getElementById('todo-input');
const addBtn = document.getElementById('add-btn');
const todoList = document.getElementById('todo-list');

// Event Handler: Adding an item
addBtn.addEventListener('click', function() {
    const taskText = todoInput.value.trim();
    if (taskText === '') return alert('Please enter a task!');

    // DOM Manipulation: Create elements dynamically
    const li = document.createElement('li');
    li.textContent = taskText;

    const deleteBtn = document.createElement('button');
    deleteBtn.textContent = '❌';
    deleteBtn.classList.add('delete-btn');

    // Event Handler: Removing an item
    deleteBtn.addEventListener('click', function() {
        todoList.removeChild(li);
    });

    li.appendChild(deleteBtn);
    todoList.appendChild(li);

    todoInput.value = ''; // Clear input field
});