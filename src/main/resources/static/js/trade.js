const sellValueInput = document.getElementById("sellValueInput");
const sellTradeableSelect = document.getElementById("sellTradeableSelect");
const sellTradeableSelectText = document.getElementById("sellTradeableSelectText");
const buyValueInput = document.getElementById("buyValueInput");
const buyTradeableSelect = document.getElementById("buyTradeableSelect");
const submitButton = document.getElementById("submitButton");

sellTradeableSelect.selectedIndex = null;
buyTradeableSelect.selectedIndex = null;

sellTradeableSelect.addEventListener("change", onSellSelectChanged);
sellValueInput.addEventListener("change", onSellInputChanged);
buyTradeableSelect.addEventListener("change", onBuySelectChanged);
buyValueInput.addEventListener("change", onBuyInputChanged);
submitButton.addEventListener("click", onSubmitClicked);

let tradeables = {};
const exchangeId = Number(document.getElementById("exchangeIdInput").value);
const accountId = Number(document.getElementById("accountIdInput").value);
refreshTradeables();
window.setInterval(refreshTradeables, 10000);

function refreshTradeables() {
    fetch("/api/exchanges/" + exchangeId + "/tradeables")
        .then((response) => {
            if (response.status !== 200) {
                console.error("Exchange API call failed.");
            } else {
                response.json().then((data) => {
                    tradeables = data;
                });
            }
        });
}

function getSelectedSellOption() {
    return sellTradeableSelect.options[sellTradeableSelect.selectedIndex];
}

function getSelectedBuyOption() {
    return buyTradeableSelect.options[buyTradeableSelect.selectedIndex];
}

function updateInputSettings(input, step, min, max) {
    input.setAttribute("step", step);
    input.setAttribute("min", min);
    input.setAttribute("max", max);
}

function resetValueInputs() {
    sellValueInput.value = null;
    buyValueInput.value = null;
}

// Event handlers

function onSellSelectChanged() {
    resetValueInputs();
    const sellOption = getSelectedSellOption();
    const sellType = sellOption.dataset.type;
    const sellBalance = Number(sellOption.dataset.balance);

    // Don't allow the user to buy the same thing they're selling.
    for (let i = 0; i < buyTradeableSelect.length; i++) {
        const buyOption = buyTradeableSelect.options[i];
        const buyType = buyOption.dataset.type;

        let isOptionDisabled = buyOption.value === sellOption.value;
        if (sellType === "STOCK" && buyType === "STOCK") {
            isOptionDisabled = true;
        }

        if (isOptionDisabled) {
            buyOption.setAttribute("disabled", true);
        } else {
            buyOption.removeAttribute("disabled");
        }
        // If the user is currently selecting an invalid choice, reset.
        if (i === buyTradeableSelect.selectedIndex && isOptionDisabled) {
            buyTradeableSelect.value = "";
        }
    }

    // Update the sell value input for the current type's parameters.
    if (sellType === "STOCK") {
        updateInputSettings(sellValueInput, 1, 0, sellBalance);
        buyValueInput.setAttribute("readonly", true);
    } else if (sellType === "FIAT" || sellType === "CRYPTO") {
        updateInputSettings(sellValueInput, 0.0000000001, 0, sellBalance);
        buyValueInput.removeAttribute("readonly");
    }

    // Update the subtext to show the value.
    sellTradeableSelectText.innerText = `Balance: ${sellBalance}`;
    sellTradeableSelectText.removeAttribute("hidden");
}

function onBuySelectChanged() {
    resetValueInputs();
    const buyOption = getSelectedBuyOption();
    const buyType = buyOption.dataset.type;

    if (buyType === "STOCK") {
        updateInputSettings(buyValueInput, 1, 0, 1000000);
        sellValueInput.setAttribute("readonly", true);
    } else if (buyType === "FIAT" || buyType === "CRYPTO") {
        updateInputSettings(buyValueInput, 0.0000000001, 0, 1000000);
        sellValueInput.removeAttribute("readonly");
    }
}

function onSellInputChanged() {
    const sellOption = getSelectedSellOption();
    const buyOption = getSelectedBuyOption();
    const sellId = sellOption.value;
    const buyId = buyOption.value;
    const sellPriceUsd = tradeables[sellId];
    const buyPriceUsd = tradeables[buyId];

    if (sellPriceUsd && buyPriceUsd) {
        const sellVolume = Number(sellValueInput.value);
        buyValueInput.value = (sellPriceUsd * sellVolume) / buyPriceUsd;
    }
}

function onBuyInputChanged() {
    const sellOption = getSelectedSellOption();
    const buyOption = getSelectedBuyOption();
    const sellId = sellOption.value;
    const buyId = buyOption.value;
    const sellPriceUsd = tradeables[sellId];
    const buyPriceUsd = tradeables[buyId];

    if (sellPriceUsd && buyPriceUsd) {
        const buyVolume = Number(buyValueInput.value);
        sellValueInput.value = (buyPriceUsd * buyVolume) / sellPriceUsd;
    }
}

function onSubmitClicked() {
    let type = "SELL";
    let value = Number(sellValueInput.value);
    if (sellValueInput.hasAttribute("readonly")) {
        type = "BUY";
        value = Number(buyValueInput.value);
    }
    const sellTradeableId = parseInt(getSelectedSellOption().value);
    const buyTradeableId = parseInt(getSelectedBuyOption().value);
    const form = document.getElementById("tradeForm");
    form.elements["type"].value = type;
    form.elements["sellTradeableId"].value = sellTradeableId;
    form.elements["buyTradeableId"].value = buyTradeableId;
    form.elements["value"].value = value;
    form.submit();
}
