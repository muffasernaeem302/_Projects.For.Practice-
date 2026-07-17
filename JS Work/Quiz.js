// Function that explicitly returns a Promise to simulate an API request
function mockFetchQuestion() {
    return new Promise((resolve, reject) => {
        // Simulating network delay
        setTimeout(() => {
            const success = true; // Simulating a successful response
            if (success) {
                resolve({
                    question: "Which language runs natively in a web browser?",
                    options: ["Java", "Python", "JavaScript", "C++"],
                    answer: "JavaScript"
                });
            } else {
                reject("Failed to load question.");
            }
        }, 1500); 
    });
}

const fetchBtn = document.getElementById('fetch-btn');
const questionText = document.getElementById('question-text');
const optionsContainer = document.getElementById('options-container');
const loadingText = document.getElementById('loading-text');
const feedback = document.getElementById('feedback');

fetchBtn.addEventListener('click', () => {
    loadingText.textContent = "Loading question from API Promise...";
    questionText.textContent = "";
    optionsContainer.innerHTML = "";
    feedback.textContent = "";

    // Consuming the Promise using .then() and .catch()
    mockFetchQuestion()
        .then((data) => {
            loadingText.textContent = "";
            questionText.textContent = data.question;

            data.options.forEach(option => {
                const button = document.createElement('button');
                button.textContent = option;
                button.style.margin = "5px";
                button.style.padding = "10px";
                
                button.addEventListener('click', () => {
                    if (option === data.answer) {
                        feedback.textContent = "🎉 Correct Answer!";
                        feedback.style.color = "green";
                    } else {
                        feedback.textContent = "❌ Wrong Answer, try again!";
                        feedback.style.color = "red";
                    }
                });
                optionsContainer.appendChild(button);
            });
        })
        .catch((error) => {
            loadingText.textContent = error;
        });
});