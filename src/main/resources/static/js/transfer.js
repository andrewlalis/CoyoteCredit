const tradeableSelect = document.getElementById("tradeableSelect");
const valueInput = document.getElementById("amountInput");

tradeableSelect.addEventListener("change", onSelectChanged);

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
