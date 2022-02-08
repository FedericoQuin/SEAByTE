

export function updateStatus(message, color='#000000') {
    let label = document.getElementById('status-label');
    label.innerHTML = message;
    label.style.color = color;
}
