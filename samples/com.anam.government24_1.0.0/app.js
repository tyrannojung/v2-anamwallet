// 정부24 미니앱 생명주기 정의

window.App = {
  onLaunch() {
    console.log('Government24 mini app started');
  },
  
  onShow() {
    console.log('Government24 mini app shown');
  },
  
  onHide() {
    console.log('Government24 mini app hidden');
  }
};

// Toast 메시지 표시
window.showToast = (message, type = "info") => {
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add("show");
  }, 100);

  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 300);
  }, 3000);
};

// 전역 로딩 표시
window.showLoading = (message = "처리 중...") => {
  // 기존 로더가 있으면 제거
  hideLoading();
  
  const loader = document.createElement("div");
  loader.id = "global-loader";
  loader.innerHTML = `
    <div class="loader-backdrop">
      <div class="loader-content">
        <div class="spinner">
          <div class="spinner-circle"></div>
        </div>
        <p class="loader-message">${message}</p>
      </div>
    </div>
  `;
  document.body.appendChild(loader);
  
  // 애니메이션을 위한 약간의 지연
  setTimeout(() => {
    loader.classList.add("show");
  }, 10);
};

// 로딩 숨기기
window.hideLoading = () => {
  const loader = document.getElementById("global-loader");
  if (loader) {
    loader.classList.remove("show");
    setTimeout(() => loader.remove(), 300);
  }
};