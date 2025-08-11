// 정부24 메인 페이지 로직
console.log("Government24 index page loaded");

// Check login status
let isLoggedIn = false;
let vpRequest = null; // VP 요청 저장용

// 로그인 핸들러
function handleLogin() {
  console.log("Login button clicked");

  // Create and store VP request
  vpRequest = {
    service: "Korea University",
    purpose: "Authentication for Korea University Academic Services",
    challenge: "ku_" + Date.now(),
    type: "student", // Student ID only
  };

  // VP 응답 이벤트 리스너 등록
  window.addEventListener("vpResponse", handleVPResponse, { once: true });
  window.addEventListener("vpError", handleVPError, { once: true });

  // VP 요청 (바텀시트 표시)
  if (window.anam && window.anam.requestVP) {
    window.anam.requestVP(JSON.stringify(vpRequest));
  } else {
    console.error("VP request function not available");
    showToast("Authentication feature not available", "error");
  }
}

// VP 응답 처리
function handleVPResponse(event) {
  console.log("VP response received:", event.detail);

  // 사용자가 학생증을 선택했으므로 이제 로딩 표시
  showLoading("Verifying student ID...");

  // event.detail이 이미 VP 객체임 (JSON 파싱 불필요)
  const vpData = event.detail;

  // VP 검증 요청 보내기
  verifyVP(vpData);
}

// VP 검증
async function verifyVP(vpData) {
  try {

    // 서버에 VP 검증 요청
    const response = await fetch("http://10.0.2.2:8081/vps/verify", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        challenge: vpRequest.challenge,
        vp: vpData,
      }),
    });

    const result = await response.json();

    if (result.valid) {
      // 검증 성공 - 로그인 처리
      isLoggedIn = true;
      hideLoading();
      showToast("Identity verified successfully", "success");

      // 결제 페이지로 이동
      setTimeout(() => {
        window.anam.navigateTo("pages/payment/payment");
      }, 1000);
    } else {
      // 검증 실패
      hideLoading();
      showToast("Identity verification failed: " + result.reason, "error");
    }
  } catch (error) {
    console.error("VP verification error:", error);
    hideLoading();
    showToast("Verification error: " + error.message, "error");
  }
}

// VP 에러 처리
function handleVPError(event) {
  console.log("VP error:", event.detail);

  const error = event.detail.error || "Authentication failed";

  if (error === "User cancelled") {
    showToast("Authentication cancelled", "info");
  } else {
    showToast("Authentication failed: " + error, "error");
  }
}

// 서비스 클릭 핸들러
function handleServiceClick(serviceName) {
  console.log(`Service clicked: ${serviceName}`);

  // Check login status
  if (!isLoggedIn) {
    // 로그인하지 않았으면 아무 반응 없음
    return;
  }

  // 주민등록등본 클릭 시 결제 페이지로 이동
  if (serviceName === "Resident Registration Transcript") {
    window.anam.navigateTo("pages/payment/payment");
  } else {
    // 다른 서비스들도 아무 반응 없음
    return;
  }
}
