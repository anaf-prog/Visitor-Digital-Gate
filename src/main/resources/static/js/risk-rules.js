function showDeleteModal(button) {
    const ruleId = button.getAttribute('data-rule-id');
    const ruleName = button.getAttribute('data-rule-name');
    document.getElementById('deleteRuleName').textContent = ruleName;
    document.getElementById('deleteForm').action = '/risk-rules/' + ruleId + '/delete';
    document.getElementById('deleteModal').style.display = 'block';
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('deleteModal');
    if (event.target == modal) {
        closeDeleteModal();
    }
}