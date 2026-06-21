// Mengambil referensi ke elemen-elemen HTML yang akan dimanipulasi
const historyBody = document.getElementById('historyBody');
const deleteModal = document.getElementById('deleteModal');
const photoModal = document.getElementById('photoModal');
const confirmDeleteBtn = document.getElementById('confirmDelete');
const cancelDeleteBtn = document.getElementById('cancelDelete');
const closePhotoBtn = document.getElementById('closePhotoBtn');
const photoPreview = document.getElementById('photoPreview');
const recordCount = document.getElementById('recordCount');

// Filter elements
const filterNama = document.getElementById('filterNama');
const filterNik = document.getElementById('filterNik');
const filterTujuan = document.getElementById('filterTujuan');
const filterCheckin = document.getElementById('filterCheckin');
const applyFilterBtn = document.getElementById('applyFilterBtn');
const clearFilterBtn = document.getElementById('clearFilterBtn');

// Pagination variables
let historyCurrentPage = 1;
const historyPageSize = 15;
let allHistoryData = [];
let filteredData = [];
let currentFilters = {};

let deleteLogId = null;

function riskClass(level) {
    return `risk-${level || 'GREEN'}`;
}

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const date = new Date(dateTimeStr);
    return date.toLocaleString('id-ID', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Fungsi untuk mengambil hanya bagian jam dari string tanggal
 * @param {string} dateStr - String tanggal/waktu
 * @returns {string} Format jam HH:MM
 */
function formatTime(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
}

// Fungsi utama untuk merender data history ke dalam tabel dengan filter dan pagination
function renderHistoryRows(rows) {
    allHistoryData = rows || [];
    filteredData = applyFilters(allHistoryData);
    
    if (filteredData.length === 0) {
        if (Object.keys(currentFilters).length > 0) {
            historyBody.innerHTML = '<tr><td colspan="9">Tidak ada data yang sesuai dengan filter.</td></tr>';
        } else {
            historyBody.innerHTML = '<tr><td colspan="9">Tidak ada data visitor dalam 30 hari terakhir.</td></tr>';
        }
        document.getElementById('historyPagination').style.display = 'none';
        recordCount.textContent = '0 data';
        return;
    }

    const totalPages = Math.ceil(filteredData.length / historyPageSize);
    const startIndex = (historyCurrentPage - 1) * historyPageSize;
    const endIndex = Math.min(startIndex + historyPageSize, filteredData.length);
    const pageData = filteredData.slice(startIndex, endIndex);

    historyBody.innerHTML = pageData.map(row => `
        <tr>
            <td>${row.fullName}</td>
            <td>${row.nik}</td>
            <td>${formatDateTime(row.checkinTime)}</td>
            <td>${row.purpose}</td>
            <td>${formatDateTime(row.checkoutTime)}</td>
            <td class="${riskClass(row.riskLevel)}">${row.riskLevel}</td>
            <td>${row.riskScore ?? '-'}</td>
            <td>
                ${row.photoUrl ? 
                    `<img src="${row.photoUrl}" alt="Foto" class="photo-preview" data-id="${row.logId}" onclick="showPhoto('${row.photoUrl}')">` : 
                    '<span style="color: #9ca3af; font-size: 12px;">Tidak ada</span>'
                }
            </td>
            <td>
                <div class="action-buttons">
                    <button type="button" class="delete-btn" data-id="${row.logId}">Hapus</button>
                </div>
            </td>
        </tr>
    `).join('');

    recordCount.textContent = `${filteredData.length} data`;
    updateHistoryPagination(totalPages);
}

function applyFilters(data) {
    let filtered = [...data];
    
    if (currentFilters.nama) {
        filtered = filtered.filter(row => 
            row.fullName.toLowerCase().includes(currentFilters.nama.toLowerCase())
        );
    }
    
    if (currentFilters.nik) {
        filtered = filtered.filter(row => 
            row.nik.includes(currentFilters.nik)
        );
    }
    
    if (currentFilters.tujuan) {
        filtered = filtered.filter(row => 
            row.purpose.toLowerCase().includes(currentFilters.tujuan.toLowerCase())
        );
    }
    
    if (currentFilters.checkin) {
        filtered = filtered.filter(row => {
            if (!row.checkinTime) return false;
            const checkinTime = formatTime(row.checkinTime);
            return checkinTime === currentFilters.checkin;
        });
    }
    
    return filtered;
}

function updateFilters() {
    currentFilters = {};
    
    if (filterNama.value.trim()) {
        currentFilters.nama = filterNama.value.trim();
    }
    
    if (filterNik.value.trim()) {
        currentFilters.nik = filterNik.value.trim();
    }
    
    if (filterTujuan.value.trim()) {
        currentFilters.tujuan = filterTujuan.value.trim();
    }
    
    if (filterCheckin.value) {
        currentFilters.checkin = filterCheckin.value;
    }
}

function clearFilters() {
    filterNama.value = '';
    filterNik.value = '';
    filterTujuan.value = '';
    filterCheckin.value = '';
    currentFilters = {};
    historyCurrentPage = 1;
}

async function refreshHistory() {
    try {
        const response = await fetch('/api/visitors/history');
        if (!response.ok) {
            throw new Error('Gagal memuat data');
        }
        const rows = await response.json();
        renderHistoryRows(rows);
    } catch (error) {
        console.error('Error loading history:', error);
        historyBody.innerHTML = '<tr><td colspan="9">Gagal memuat data. Silakan coba lagi.</td></tr>';
        recordCount.textContent = 'Error memuat data';
    }
}

function updateHistoryPagination(totalPages) {
    const paginationContainer = document.getElementById('historyPagination');
    
    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }
    
    paginationContainer.style.display = 'flex';
    
    // Update page info
    const startItem = (historyCurrentPage - 1) * historyPageSize + 1;
    const endItem = Math.min(historyCurrentPage * historyPageSize, filteredData.length);
    document.getElementById('historyPageInfo').textContent = 
        `Menampilkan ${startItem}-${endItem} dari ${filteredData.length} data`;
    
    // Update buttons
    const prevBtn = document.getElementById('historyPrevBtn');
    const nextBtn = document.getElementById('historyNextBtn');
    
    prevBtn.disabled = historyCurrentPage === 1;
    nextBtn.disabled = historyCurrentPage === totalPages;
    
    // Update page numbers
    const pageNumbersContainer = document.getElementById('historyPageNumbers');
    pageNumbersContainer.innerHTML = '';
    
    const maxVisiblePages = 5;
    let startPage = Math.max(1, historyCurrentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
    
    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `page-number ${i === historyCurrentPage ? 'active' : ''}`;
        pageBtn.textContent = i;
        pageBtn.onclick = () => {
            historyCurrentPage = i;
            renderHistoryRows(allHistoryData);
        };
        pageNumbersContainer.appendChild(pageBtn);
    }
}

function showPhoto(photoUrl) {
    photoPreview.src = photoUrl;
    photoModal.style.display = 'block';
}

// Event Listeners
document.getElementById('refreshBtn').addEventListener('click', refreshHistory);

applyFilterBtn.addEventListener('click', () => {
    updateFilters();
    historyCurrentPage = 1;
    renderHistoryRows(allHistoryData);
});

clearFilterBtn.addEventListener('click', () => {
    clearFilters();
    renderHistoryRows(allHistoryData);
});

// Pagination event listeners
document.getElementById('historyPrevBtn').addEventListener('click', () => {
    if (historyCurrentPage > 1) {
        historyCurrentPage--;
        renderHistoryRows(allHistoryData);
    }
});

document.getElementById('historyNextBtn').addEventListener('click', () => {
    const totalPages = Math.ceil(filteredData.length / historyPageSize);
    if (historyCurrentPage < totalPages) {
        historyCurrentPage++;
        renderHistoryRows(allHistoryData);
    }
});

// Delete functionality
historyBody.addEventListener('click', async function (event) {
    const target = event.target;
    if (!target.classList.contains('delete-btn')) {
        return;
    }

    deleteLogId = target.getAttribute('data-id');
    deleteModal.style.display = 'block';
});

confirmDeleteBtn.addEventListener('click', async function () {
    if (deleteLogId) {
        try {
            const response = await fetch(`/api/visitors/${deleteLogId}`, { method: 'DELETE' });
            if (!response.ok) {
                throw new Error('Gagal menghapus data');
            }
            deleteModal.style.display = 'none';
            deleteLogId = null;
            await refreshHistory();
        } catch (error) {
            console.error('Error deleting record:', error);
            alert('Gagal menghapus data. Silakan coba lagi.');
        }
    }
});

cancelDeleteBtn.addEventListener('click', function () {
    deleteModal.style.display = 'none';
    deleteLogId = null;
});

closePhotoBtn.addEventListener('click', function () {
    photoModal.style.display = 'none';
    photoPreview.src = '';
});

// Close modals when clicking outside
window.addEventListener('click', function (event) {
    if (event.target === deleteModal) {
        deleteModal.style.display = 'none';
        deleteLogId = null;
    }
    if (event.target === photoModal) {
        photoModal.style.display = 'none';
        photoPreview.src = '';
    }
});

// Keyboard shortcuts
document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        if (deleteModal.style.display === 'block') {
            deleteModal.style.display = 'none';
            deleteLogId = null;
        }
        if (photoModal.style.display === 'block') {
            photoModal.style.display = 'none';
            photoPreview.src = '';
        }
    }
});

// Enter key to apply filters
[filterNama, filterNik, filterTujuan, filterCheckin].forEach(input => {
    input.addEventListener('keypress', function (event) {
        if (event.key === 'Enter') {
            applyFilterBtn.click();
        }
    });
});

// Initialize page
refreshHistory();
