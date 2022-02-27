const tradeableSelect = document.getElementById("tradeableSelect");
const valueInput = document.getElementById("amountInput");
const recipientNumberInput = document.getElementById("recipientNumberInput");
const recipientNumberNote = document.getElementById("recipientNumberNote");

const exchangeId = document.getElementById("exchangeIdInput").value;
const accountNumberRegex = new RegExp("\\d{4}-\\d{4}-\\d{4}-\\d{4}");

tradeableSelect.addEventListener("change", onSelectChanged);
recipientNumberInput.addEventListener("change", onAccountNumberChanged);
recipientNumberInput.addEventListener("keyup", onAccountNumberChanged);

function onSelectChanged() {
    valueInput.value = null;
    const option = tradeableSelect.options[tradeableSelect.selectedIndex];
    const type = option.dataset.type;
    const balance = Number(option.dataset.amount);
    valueInput.setAttribute("max", "" + balance);
    if (type === "STOCK") {
        valueInput.setAttribute("step", 1);
        valueInput.setAttribute("min", 1);
    } else {
        valueInput.setAttribute("step", 0.0000000001);
        valueInput.setAttribute("min", 0.0000000001);
    }
}

/**
 * Check that the account number that was entered is valid.
 */
function onAccountNumberChanged() {
    const currentNumber = recipientNumberInput.value;
    console.log("Account number changed to " + currentNumber);
    if (currentNumber !== undefined && currentNumber.length > 0) {
        if (accountNumberRegex.test(currentNumber)) {
            recipientNumberNote.innerText = "Valid account number.";
            fetch("/api/exchanges/" + exchangeId + "/accounts/" + currentNumber)
                .then(response => {
                    if (response.status === 200) {
                        response.json()
                            .then(data => {
                                console.log(data);
                                showRecipientNumberNote("info", "Account number is valid. Will transfer to <span class='fw-bold'>" + data.name + "</span>");
                            })
                            .catch(() => {
                                showRecipientNumberNote("warning", "Could not find an account with that number.");
                            });
                    } else if (response.status === 404) {
                        showRecipientNumberNote("warning", "Could not find an account with that number.");
                    } else {
                        showRecipientNumberNote("warning", "Error: Couldn't read API response.");
                    }
                })
                .catch(error => {
                    console.log(error);
                    showRecipientNumberNote("warning", "API error occurred. Couldn't fetch account information.");
                });
        } else {
            showRecipientNumberNote("warning", "Invalid account number. Format: <span class='monospace'>1234-1234-1234-1234</span>");
        }
    } else {
        showRecipientNumberNote("info", "Enter an account number.");
    }
}

function showRecipientNumberNote(type, text) {
    recipientNumberNote.innerHTML = text;
    if (type === "warning") {
        recipientNumberNote.classList.add("text-danger");
        recipientNumberNote.classList.remove("text-muted");
    } else {
        recipientNumberNote.classList.remove("text-danger");
        recipientNumberNote.classList.add("text-muted");
    }
}
