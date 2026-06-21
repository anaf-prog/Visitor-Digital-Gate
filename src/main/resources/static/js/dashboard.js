// Mengambil referensi ke elemen-elemen HTML yang akan dimanipulasi
const activeBody = document.getElementById('activeBody');
const summaryArea = document.getElementById('summaryArea');
const formMessage = document.getElementById('formMessage');
const todayBody = document.getElementById('todayBody');
const deleteModal = document.getElementById('deleteModal');
const confirmDeleteBtn = document.getElementById('confirmDelete');
const cancelDeleteBtn = document.getElementById('cancelDelete');
const nikInput = document.getElementById('nik');
let deleteLogId = null; // Menyimpan ID log yang akan dihapus

// Pagination variables
let activeCurrentPage = 1;
let todayCurrentPage = 1;
const activePageSize = 10;
const todayPageSize = 15;
let allActiveData = [];
let allTodayData = [];

/**
 * Fungsi untuk menentukan kelas CSS berdasarkan level risiko
 * @param {string} level - Level risiko (GREEN, YELLOW, RED)
 */
function riskClass(level) {
    return `risk-${level || 'GREEN'}`;
}

/**
 * Fungsi untuk validasi format NIK (harus 16 digit angka)
 * @param {string} nik - NIK yang akan divalidasi
 * @returns {boolean} True jika valid, false jika tidak
 */
function isNikValid(nik) {
    return /^\d{16}$/.test(nik);
}

// Fungsi untuk merender data tamu aktif ke dalam tabel dengan pagination
function renderActiveRows(rows) {
    allActiveData = rows || [];
    
    if (allActiveData.length === 0) {
        activeBody.innerHTML = '<tr><td colspan="6">Belum ada tamu di area.</td></tr>';
        document.getElementById('activePagination').style.display = 'none';
        return;
    }

    // Hitung total halaman dan ambil data untuk halaman saat ini
    const totalPages = Math.ceil(allActiveData.length / activePageSize);
    const startIndex = (activeCurrentPage - 1) * activePageSize;
    const endIndex = Math.min(startIndex + activePageSize, allActiveData.length);
    const pageData = allActiveData.slice(startIndex, endIndex);

    activeBody.innerHTML = pageData.map(row => `
        <tr>
            <td>${row.fullName}</td>
            <td>${row.nik}</td>
            <td>${row.purpose}</td>
            <td class="${riskClass(row.riskLevel)}">${row.riskLevel}</td>
            <td>${row.riskScore ?? '-'}</td>
            <td><button type="button" class="checkout-btn" data-id="${row.logId}">Checkout</button></td>
        </tr>
    `).join('');

    updateActivePagination(totalPages);
}

// Fungsi async untuk mengambil data tamu aktif dari server dan merendernya
async function refreshActive() {
    const response = await fetch('/api/visitors/active');
    const rows = await response.json();
    renderActiveRows(rows);
}

/**
 * Fungsi untuk memformat tanggal/waktu ke format lokal Indonesia
 * @param {string} dateTimeStr - String tanggal/waktu
 * @returns {string} Tanggal/waktu yang sudah diformat
 */
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

//  Fungsi untuk merender data riwayat kunjungan hari ini ke dalam tabel
function renderTodayRows(rows) {
    allTodayData = rows || [];
    
    if (allTodayData.length === 0) {
        todayBody.innerHTML = '<tr><td colspan="8">Tidak ada data visitor hari ini.</td></tr>';
        document.getElementById('todayPagination').style.display = 'none';
        return;
    }

    const totalPages = Math.ceil(allTodayData.length / todayPageSize);
    const startIndex = (todayCurrentPage - 1) * todayPageSize;
    const endIndex = Math.min(startIndex + todayPageSize, allTodayData.length);
    const pageData = allTodayData.slice(startIndex, endIndex);

    todayBody.innerHTML = pageData.map(row => `
        <tr>
            <td>${row.fullName}</td>
            <td>${row.nik}</td>
            <td>${formatDateTime(row.checkinTime)}</td>
            <td>${row.purpose}</td>
            <td>${formatDateTime(row.checkoutTime)}</td>
            <td class="${riskClass(row.riskLevel)}">${row.riskLevel}</td>
            <td>${row.riskScore ?? '-'}</td>
            <td><button type="button" class="delete-btn" data-id="${row.logId}">Hapus</button></td>
        </tr>
    `).join('');

    updateTodayPagination(totalPages);
}

//  Fungsi async untuk mengambil data riwayat hari ini dari server
async function refreshToday() {
    const response = await fetch('/api/visitors/today');
    const rows = await response.json();
    renderTodayRows(rows);
}

// Fungsi untuk memperbarui kontrol pagination pada tabel tamu aktif
function updateActivePagination(totalPages) {
    const paginationContainer = document.getElementById('activePagination');
    
    // Sembunyikan pagination jika hanya 1 halaman
    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }
    
    paginationContainer.style.display = 'flex';
    
    // Update informasi halaman
    const startItem = (activeCurrentPage - 1) * activePageSize + 1;
    const endItem = Math.min(activeCurrentPage * activePageSize, allActiveData.length);
    document.getElementById('activePageInfo').textContent = 
        `Menampilkan ${startItem}-${endItem} dari ${allActiveData.length} data`;
    
    // Update status tombol prev/next
    const prevBtn = document.getElementById('activePrevBtn');
    const nextBtn = document.getElementById('activeNextBtn');
    
    prevBtn.disabled = activeCurrentPage === 1;
    nextBtn.disabled = activeCurrentPage === totalPages;
    
    // Generate tombol nomor halaman
    const pageNumbersContainer = document.getElementById('activePageNumbers');
    pageNumbersContainer.innerHTML = '';
    
    const maxVisiblePages = 5;
    let startPage = Math.max(1, activeCurrentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
    
    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `page-number ${i === activeCurrentPage ? 'active' : ''}`;
        pageBtn.textContent = i;
        pageBtn.onclick = () => {
            activeCurrentPage = i;
            renderActiveRows(allActiveData);
        };
        pageNumbersContainer.appendChild(pageBtn);
    }
}

// Fungsi untuk memperbarui kontrol pagination pada tabel riwayat hari ini
function updateTodayPagination(totalPages) {
    const paginationContainer = document.getElementById('todayPagination');
    
    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }
    
    paginationContainer.style.display = 'flex';
    
    // Update page info
    const startItem = (todayCurrentPage - 1) * todayPageSize + 1;
    const endItem = Math.min(todayCurrentPage * todayPageSize, allTodayData.length);
    document.getElementById('todayPageInfo').textContent = 
        `Menampilkan ${startItem}-${endItem} dari ${allTodayData.length} data`;
    
    // Update buttons
    const prevBtn = document.getElementById('todayPrevBtn');
    const nextBtn = document.getElementById('todayNextBtn');
    
    prevBtn.disabled = todayCurrentPage === 1;
    nextBtn.disabled = todayCurrentPage === totalPages;
    
    // Update page numbers
    const pageNumbersContainer = document.getElementById('todayPageNumbers');
    pageNumbersContainer.innerHTML = '';
    
    const maxVisiblePages = 5;
    let startPage = Math.max(1, todayCurrentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages, startPage + maxVisiblePages - 1);
    
    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = Math.max(1, endPage - maxVisiblePages + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = `page-number ${i === todayCurrentPage ? 'active' : ''}`;
        pageBtn.textContent = i;
        pageBtn.onclick = () => {
            todayCurrentPage = i;
            renderTodayRows(allTodayData);
        };
        pageNumbersContainer.appendChild(pageBtn);
    }
}

// Fungsi async untuk mengambil dan menampilkan ringkasan kunjungan
async function refreshSummary() {
    const response = await fetch('/api/visitors/summary');
    const summary = await response.json();
    summaryArea.innerHTML = `
        <p>Total Kunjungan: ${summary.totalVisitToday}</p>
        <p>Masih di Area: ${summary.totalInsideArea}</p>
        <p>Green: ${summary.greenCount}, Yellow: ${summary.yellowCount}, Red: ${summary.redCount}</p>
        <p>${summary.aiSummary}</p>
    `;
}

// Handler untuk submit form check-in tamu baru
document.getElementById('registerForm').addEventListener('submit', async function (event) {
    event.preventDefault(); // Mencegah reload halaman

    const nik = nikInput.value.trim();
    if (!isNikValid(nik)) {
        formMessage.innerHTML = '<p class="error">NIK harus 16 digit angka.</p>';
        nikInput.focus();
        return;
    }

    const formData = new FormData();
    formData.append('fullName', document.getElementById('fullName').value);
    formData.append('nik', nik);
    formData.append('purpose', document.getElementById('purpose').value);
    
    const photoInput = document.getElementById('photo');
    if (photoInput.files && photoInput.files[0]) {
        formData.append('photo', photoInput.files[0]); // Upload foto jika ada
    }

    try {
        const response = await fetch('/api/visitors/register', {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('Gagal check-in. Pastikan data valid.');
        }

        const result = await response.json();
        formMessage.innerHTML = `<p class="success">Check-in berhasil. Risk ${result.riskLevel} (score ${result.riskScore}).</p>`;
        this.reset();
        await refreshActive();
        await refreshSummary();
        await refreshToday();
    } catch (error) {
        formMessage.innerHTML = `<p class="error">${error.message}</p>`;
    }
});

// Membatasi input NIK hanya angka dan maksimal 16 digit
nikInput.addEventListener('input', function () {
    const numericValue = this.value.replace(/\D/g, '').slice(0, 16);
    if (this.value !== numericValue) {
        this.value = numericValue;
    }
});

document.getElementById('refreshBtn').addEventListener('click', refreshActive);
document.getElementById('summaryBtn').addEventListener('click', refreshSummary);
document.getElementById('refreshTodayBtn').addEventListener('click', refreshToday);

// ===== EVENT LISTENER PAGINATION =====
// Navigasi halaman sebelumnya untuk tabel aktif
document.getElementById('activePrevBtn').addEventListener('click', () => {
    if (activeCurrentPage > 1) {
        activeCurrentPage--;
        renderActiveRows(allActiveData);
    }
});

// Navigasi halaman berikutnya untuk tabel aktif
document.getElementById('activeNextBtn').addEventListener('click', () => {
    const totalPages = Math.ceil(allActiveData.length / activePageSize);
    if (activeCurrentPage < totalPages) {
        activeCurrentPage++;
        renderActiveRows(allActiveData);
    }
});

document.getElementById('todayPrevBtn').addEventListener('click', () => {
    if (todayCurrentPage > 1) {
        todayCurrentPage--;
        renderTodayRows(allTodayData);
    }
});

document.getElementById('todayNextBtn').addEventListener('click', () => {
    const totalPages = Math.ceil(allTodayData.length / todayPageSize);
    if (todayCurrentPage < totalPages) {
        todayCurrentPage++;
        renderTodayRows(allTodayData);
    }
});

// ===== HANDLER CHECKOUT TAMU =====
// Event delegation untuk tombol checkout di tabel aktif
activeBody.addEventListener('click', async function (event) {
    const target = event.target;
    if (!target.classList.contains('checkout-btn')) {
        return;
    }

    const logId = target.getAttribute('data-id');
    await fetch(`/api/visitors/${logId}/checkout`, { method: 'POST' });
    await refreshActive();
    await refreshSummary();
    await refreshToday();
});

// ===== HANDLER HAPUS DATA =====
// Tampilkan modal konfirmasi saat tombol hapus diklik
todayBody.addEventListener('click', async function (event) {
    const target = event.target;
    if (!target.classList.contains('delete-btn')) {
        return;
    }

    deleteLogId = target.getAttribute('data-id');
    deleteModal.style.display = 'block';
});

// Konfirmasi penghapusan data
confirmDeleteBtn.addEventListener('click', async function () {
    if (deleteLogId) {
        await fetch(`/api/visitors/${deleteLogId}`, { method: 'DELETE' });
        deleteModal.style.display = 'none';
        deleteLogId = null;
        await refreshActive();
        await refreshSummary();
        await refreshToday();
    }
});

cancelDeleteBtn.addEventListener('click', function () {
    deleteModal.style.display = 'none';
    deleteLogId = null;
});

// Close modal when clicking outside
window.addEventListener('click', function (event) {
    if (event.target === deleteModal) {
        deleteModal.style.display = 'none';
        deleteLogId = null;
    }
});

setInterval(refreshActive, 15000);
setInterval(refreshToday, 30000);

refreshToday();