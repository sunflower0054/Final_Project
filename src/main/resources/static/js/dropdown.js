/*

function updateClock() {
    const now = new Date();
    const timeEl = document.getElementById('currentTime');
    if(!timeEl) return;
    timeEl.innerText =
        now.getFullYear() + '. ' +
        (now.getMonth() + 1).toString().padStart(2, '0') + '. ' +
        now.getDate().toString().padStart(2, '0') + ' ' +
        now.getHours().toString().padStart(2, '0') + ':' +
        now.getMinutes().toString().padStart(2, '0') + ':' +
        now.getSeconds().toString().padStart(2, '0');
}

function setupDropdown(btnId, dropdownId) {
    const btn = document.getElementById(btnId);
    const dropdown = document.getElementById(dropdownId);
    if(!btn || !dropdown) return;

    btn.addEventListener('click', (e) => {
        e.stopPropagation();
        document.querySelectorAll('.dropdown-menu').forEach(menu => {
            if (menu.id !== dropdownId) menu.classList.remove('open');
        });
        dropdown.classList.toggle('open');
    });
}

function closeDropdownsOnOutside(e) {
    if (!e.target.closest('.dropdown-wrap')) {
        document.querySelectorAll('.dropdown-menu').forEach(menu => {
            menu.classList.remove('open');
        });
    }
}

function initialize() {
    updateClock();
    setInterval(updateClock, 1000);

    setupDropdown('profileBtn', 'profileDropdown');

    document.addEventListener('click', closeDropdownsOnOutside);
}

window.addEventListener('load', initialize);
*/

// dropdown.js (수정 후)

function updateClock() {
    const now = new Date();
    const timeEl = document.getElementById('currentTime');
    if(!timeEl) return;
    timeEl.innerText =
        now.getFullYear() + '. ' +
        (now.getMonth() + 1).toString().padStart(2, '0') + '. ' +
        now.getDate().toString().padStart(2, '0') + ' ' +
        now.getHours().toString().padStart(2, '0') + ':' +
        now.getMinutes().toString().padStart(2, '0') + ':' +
        now.getSeconds().toString().padStart(2, '0');
}

function initialize() {
    // 시계 1초마다 갱신
    updateClock();
    setInterval(updateClock, 1000);
}

// 페이지가 로드될 때 실행
window.addEventListener('load', initialize);