console.log("Initial Setup Completed ");

function createCOunter() {
    let count = 0; 

    return {
        increment : function() {
            count ++;
            return count;
        },
        decrement : function() {
            count--;
            return count;
        }
    };
}

const mycounter = createCOunter();

// DOM ELEMENTS (Fixed ID case-sensitivity)
const counterDisplay = document.getElementById("counter-value");
const incrementBtn = document.getElementById("increment-btn");
const decrementbtn = document.getElementById("decrement-btn"); // Fixed 'Decrement-Btn' to 'decrement-btn'

// Event Listeners (Fixed event names and variable casing)
incrementBtn.addEventListener("click", () => { // Fixed 'Click' to 'click'
    counterDisplay.textContent = mycounter.increment(); // Fixed 'counterdisplay' to 'counterDisplay'
});

decrementbtn.addEventListener("click", () => { // Fixed 'Click' to 'click'
    counterDisplay.textContent = mycounter.decrement(); // Fixed 'counterdisplay' to 'counterDisplay'
});