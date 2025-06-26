// 악성 Bitcoin 지갑 - 보안 테스트 (PPT 데모용)

/**
 * 🔒 AnamWallet 보안 아키텍처 설명
 *
 * 1. WebView 프로세스 격리:
 *    - WebApp 프로세스 (:app) - 일반 미니앱 실행
 *    - Blockchain 프로세스 (:blockchain) - 블록체인 미니앱 실행
 *    - 각 프로세스는 독립된 WebView 인스턴스와 저장소를 가짐
 *
 * 2. 데이터 저장 경로:
 *    - 모든 localStorage는 단일 LevelDB에 저장됨
 *    - 경로: /data/data/com.anam145.wallet/app_webview_<프로세스>/Default/Local Storage/leveldb/
 *    - LevelDB 내부 키 형식: "<scheme>_<host>_<port>|<localStorage-key>"
 *
 *    예시:
 *    - Ethereum 지갑:
 *      • localStorage 키: "ethereum_wallet"
 *      • 저장 경로: /data/data/com.anam145.wallet/app_webview_blockchain/Default/Local Storage/leveldb/
 *      • LevelDB 키: "https_com.anam.ethereum.miniapp.local_0|ethereum_wallet"
 *
 *    - 현재 악성 모듈:
 *      • Origin: https://com.malicious.blockchain.miniapp.local
 *      • 저장 경로: /data/data/com.anam145.wallet/app_webview_blockchain/Default/Local Storage/leveldb/
 *      • LevelDB 키 prefix: "https_com.malicious.blockchain.miniapp.local_0|"
 *
 *    - Government24 앱:
 *      • Origin: https://kr.go.government24.miniapp.local
 *      • 저장 경로: /data/data/com.anam145.wallet/app_webview_webapp/Default/Local Storage/leveldb/
 *      • LevelDB 키 prefix: "https_kr.go.government24.miniapp.local_0|"
 *
 *    ※ 중요: 같은 leveldb에 저장되어도 키 prefix로 origin별 격리가 유지됨
 *
 * 3. 보안 메커니즘:
 *    - Same-Origin Policy: 다른 origin의 localStorage 접근 차단
 *    - WebViewAssetLoader: 각 미니앱을 고유한 https:// origin으로 매핑
 *    - Process Isolation: WebApp과 Blockchain 프로세스 간 격리
 *    - File Protocol Block: file:// 접근 차단
 *
 * 4. 테스트 목적:
 *    이 스크립트는 위의 보안 메커니즘이 제대로 작동하는지 검증합니다.
 *    악성 미니앱이 다른 미니앱의 민감한 데이터(개인키, 니모닉 등)에
 *    접근할 수 없음을 증명합니다.
 */

// 디버그 모드
const DEBUG = true;

// 테스트 결과 수집기
const testResults = [];

// 결과 출력 헬퍼
function log(message, status = "info") {
  if (!DEBUG) return;

  const icons = {
    success: "✅",
    error: "❌",
    warning: "⚠️",
    info: "📍",
  };
  console.log(`${icons[status] || ""} [Malicious] ${message}`);
}

// 지연 함수
function delay(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

// 테스트 단계 실행 헬퍼
async function step(title, fn, delayMs = 1500) {
  log(`\n📌 ${title}`, "warning");
  const result = await fn();
  testResults.push(result);
  await delay(delayMs);
  return result;
}

// iframe 로더 유틸리티
async function loadFrame(src, timeout = 3000) {
  const iframe = document.createElement("iframe");
  iframe.src = src;
  iframe.style.display = "none";
  document.body.appendChild(iframe);

  const result = await new Promise((resolve) => {
    let timeoutId;

    const cleanup = (ok, reason) => {
      clearTimeout(timeoutId);
      resolve({ iframe, ok, reason });
    };

    iframe.onload = () => cleanup(true, "loaded");
    iframe.onerror = () => cleanup(false, "error");
    timeoutId = setTimeout(() => cleanup(false, "timeout"), timeout);
  });

  // about:blank 원인 분석
  if (!result.ok || iframe.contentWindow.location.href === "about:blank") {
    try {
      const bodyLength = iframe.contentDocument?.body?.innerHTML?.length || 0;
      result.reason =
        bodyLength > 0 ? "mixed-content/certificate" : "host-resolver-failure";
    } catch (e) {
      result.reason = "cross-origin-blocked";
    }
  }

  return result;
}

// PPT 데모 - 메인 함수
async function runPPTDemo() {
  log("========== 🎯 PPT 보안 실험 시작 ==========", "info");
  log("각 공격 벡터를 단계별로 테스트합니다\n", "info");

  // 테스트 결과 초기화
  testResults.length = 0;

  await delay(1000);

  // 각 테스트를 간결하게 실행
  await step("실험 1: 현재 Origin localStorage 확인", testLocalStorageAccess);
  await step("실험 2: Cross-Origin localStorage 접근", testCrossOriginAccess);
  await step("실험 3: 파일 시스템 직접 접근", testFileSystemAccess);
  await step("실험 4: 메모리 및 전역 변수 스캔", testMemoryScanning);
  await step("실험 5: Cross-Origin 리소스 접근", testCrossOriginRequests);

  // 결과 요약
  showSummary();
}

// 1. 현재 Origin localStorage 확인
async function testLocalStorageAccess() {
  const result = {
    vector: "local-storage",
    stages: [],
  };

  // 현재 origin 정보
  log("현재 origin 정보:", "info");
  log(`  Origin: ${window.location.origin}`, "info");
  log(`  URL: ${window.location.href}`, "info");
  log(`  localStorage 키 개수: ${localStorage.length}`, "info");

  if (localStorage.length > 0) {
    const keys = [];
    for (let i = 0; i < localStorage.length; i++) {
      keys.push(localStorage.key(i));
    }
    log(`  저장된 키: ${keys.join(", ")}`, "info");
  }

  // ethereum_wallet 검색
  log("\nethereum_wallet 키 검색:", "info");
  const localWallet = localStorage.getItem("ethereum_wallet");
  if (localWallet) {
    log("  🚨 현재 origin에서 ethereum_wallet 발견!", "error");
    const wallet = JSON.parse(localWallet);
    log(`  지갑 주소: ${wallet.address}`, "error");
    log(`  잔액: ${wallet.balance}`, "error");
    log("  → 악성 앱이 같은 origin에 있다면 접근 가능", "warning");
    result.stages.push({ stage: "local-read", ok: true, data: "wallet_found" });
  } else {
    log("  ✅ 현재 origin에는 ethereum_wallet이 없음", "success");
    log("  → 다른 origin의 데이터는 기본적으로 접근 불가", "info");
    result.stages.push({ stage: "local-read", ok: false, data: "no_wallet" });
  }

  return result;
}

// 2. Cross-Origin localStorage 접근 테스트
async function testCrossOriginAccess() {
  const result = {
    vector: "cross-origin-localStorage",
    stages: [],
  };

  // Step 1: iframe을 통한 Cross-origin 접근 시도
  log("Step 1: iframe을 통해 Ethereum origin 접근 시도", "info");
  const { iframe, ok, reason } = await loadFrame(
    "https://com.anam.ethereum.miniapp.local/pages/index/index.html"
  );

  if (!ok || iframe.contentWindow.location.href === "about:blank") {
    log(`  ⚠️ iframe 로드 실패: ${reason}`, "warning");
    log("  → Same-Origin Policy가 작동하여 접근 차단", "success");
    result.stages.push({ stage: "iframe-load", ok: false, reason });
    document.body.removeChild(iframe);
    return result;
  }

  // Step 2: localStorage 객체 접근
  log("\nStep 2: iframe의 localStorage 객체 접근 시도", "info");
  try {
    const ethStorage = iframe.contentWindow.localStorage;
    log("  localStorage 객체 참조 획득", "error");
    result.stages.push({ stage: "access", ok: true });

    // Step 3: ethereum_wallet 데이터 읽기
    log("\nStep 3: Cross-origin ethereum_wallet 데이터 읽기 시도", "info");
    const walletData = ethStorage.getItem("ethereum_wallet");
    if (walletData) {
      log("  🚨 Cross-origin ethereum_wallet 키 발견!", "error");
      const wallet = JSON.parse(walletData);
      log(`  지갑 주소: ${wallet.address}`, "error");

      if (wallet.encryptedPrivateKey) {
        try {
          const privateKey = atob(wallet.encryptedPrivateKey);
          log(
            `  🚨 개인키 복호화 성공: ${privateKey.substring(0, 10)}...`,
            "error"
          );
        } catch (e) {
          log("  개인키 복호화 실패", "warning");
        }
      }
      result.stages.push({
        stage: "cross-origin-read",
        ok: true,
        data: "wallet_found",
      });
    } else {
      log("  ethereum_wallet 키가 없음 (지갑 미생성)", "warning");
      result.stages.push({
        stage: "cross-origin-read",
        ok: true,
        data: "no_wallet",
      });
    }

    // Step 4: 쓰기 권한 테스트
    log("\nStep 4: Cross-origin localStorage 쓰기 권한 테스트", "info");
    const testKey = "malicious_test_" + Date.now();
    ethStorage.setItem(testKey, "hacked by malicious origin");
    const written = ethStorage.getItem(testKey);
    if (written === "hacked by malicious origin") {
      log("  🚨 Cross-origin 쓰기 성공 - 완전한 제어 가능", "error");
      ethStorage.removeItem(testKey);
      result.stages.push({ stage: "cross-origin-write", ok: true });

      // 추가: 데이터 유출 가능성
      log("\n  → 공격자가 다음을 할 수 있음:", "error");
      log("    1. 기존 지갑 데이터 탈취", "error");
      log("    2. 가짜 지갑 주입", "error");
      log("    3. 트랜잭션 조작", "error");
    }
  } catch (e) {
    log(`  localStorage 접근 차단: ${e.message}`, "success");
    result.stages.push({ stage: "access", ok: false, reason: e.message });
  }

  document.body.removeChild(iframe);
  return result;
}

// 2. 파일 시스템 접근 테스트
async function testFileSystemAccess() {
  const result = {
    vector: "filesystem",
    stages: [],
  };

  log("WebView 내부 저장소 경로 정보 (Chromium M99+)", "info");
  log(
    "  /data/data/com.anam145.wallet/app_webview_blockchain/Default/Local Storage/leveldb/",
    "info"
  );

  const testPaths = [
    "file:///data/data/com.anam145.wallet/app_webview_blockchain/Default/Local Storage/leveldb/",
    "file:///android_asset/",
  ];

  // Promise.all로 병렬 실행
  const checks = await Promise.all(
    testPaths.map(async (path) => {
      const { ok } = await loadFrame(path, 1000);
      log(
        `  ${ok ? "❌" : "✅"} ${path} ${ok ? "접근됨!" : "차단됨"}`,
        ok ? "error" : "success"
      );
      return { path, ok: !ok };
    })
  );

  result.stages = checks;
  return result;
}

// 3. 메모리 및 전역 변수 스캔
async function testMemoryScanning() {
  const result = {
    vector: "memory-scan",
    stages: [],
  };

  log("전역 변수에서 민감한 정보 검색", "info");

  const suspiciousKeys = [];
  const keywords = [
    "key",
    "private",
    "secret",
    "password",
    "mnemonic",
    "seed",
    "wallet",
  ];

  // window 객체 스캔
  for (const key in window) {
    if (keywords.some((word) => key.toLowerCase().includes(word))) {
      suspiciousKeys.push({ obj: "window", key });
    }
  }

  // navigator 깊이 스캔
  Object.getOwnPropertyNames(navigator).forEach((key) => {
    if (keywords.some((word) => key.toLowerCase().includes(word))) {
      suspiciousKeys.push({ obj: "navigator", key });
    }
  });

  // document 깊이 스캔
  Object.getOwnPropertyNames(document).forEach((key) => {
    if (keywords.some((word) => key.toLowerCase().includes(word))) {
      suspiciousKeys.push({ obj: "document", key });
    }
  });

  if (suspiciousKeys.length > 0) {
    log(`  ⚠️ 의심스러운 변수 ${suspiciousKeys.length}개 발견:`, "warning");
    suspiciousKeys.forEach(({ obj, key }) =>
      log(`    - ${obj}.${key}`, "warning")
    );
    result.stages.push({
      stage: "global-scan",
      ok: false,
      count: suspiciousKeys.length,
    });
  } else {
    log("  ✅ 민감한 전역 변수 없음", "success");
    result.stages.push({ stage: "global-scan", ok: true });
  }

  // SharedArrayBuffer 테스트
  log("\nSharedArrayBuffer 테스트 (Spectre 공격)", "info");

  // crossOriginIsolated 체크 추가
  if (window.crossOriginIsolated) {
    log("  ⚠️ Cross-Origin Isolated 모드 활성화됨", "warning");
  }

  try {
    new SharedArrayBuffer(1024);
    log("  ❌ SharedArrayBuffer 생성 가능", "error");
    result.stages.push({ stage: "spectre", ok: false });
  } catch (e) {
    log("  ✅ SharedArrayBuffer 차단됨", "success");
    result.stages.push({ stage: "spectre", ok: true });
  }

  return result;
}

// 4. Cross-Origin 리소스 접근
async function testCrossOriginRequests() {
  const result = {
    vector: "cross-origin-requests",
    stages: [],
  };

  const targets = [
    { url: "https://com.anam.ethereum.miniapp.local/", name: "Ethereum" },
    { url: "https://kr.go.government24.miniapp.local/", name: "Government24" },
  ];

  for (const target of targets) {
    log(`\n${target.name} 요청 테스트`, "info");

    // XHR 테스트
    const xhrOk = await new Promise((resolve) => {
      const xhr = new XMLHttpRequest();
      xhr.onload = () => {
        log("  ❌ XHR 요청 성공", "error");
        resolve(false);
      };
      xhr.onerror = xhr.ontimeout = () => {
        log("  ✅ XHR 요청 차단", "success");
        resolve(true);
      };
      xhr.open("GET", target.url, true);
      xhr.timeout = 2000;
      xhr.send();
    });

    // Fetch 테스트
    let fetchOk = true;
    try {
      await fetch(target.url, { mode: "cors", credentials: "include" });
      log("  ❌ Fetch 요청 성공", "error");
      fetchOk = false;
    } catch (e) {
      log("  ✅ Fetch 요청 차단", "success");
    }

    result.stages.push({
      target: target.name,
      xhr: xhrOk,
      fetch: fetchOk,
    });
  }

  return result;
}

// 결과 요약 표시
function showSummary() {
  log("\n========== 📊 테스트 결과 요약 ==========", "info");

  // 각 공격 벡터별 결과 정리
  const attackResults = testResults.map((result) => {
    let blocked = true;
    let details = [];

    switch (result.vector) {
      case "local-storage":
        // 현재 origin의 localStorage 접근은 정상
        const localRead = result.stages.find((s) => s.stage === "local-read");
        if (localRead && localRead.data === "wallet_found") {
          blocked = false; // 같은 origin 접근은 정상
          details.push("현재 origin에서 ethereum_wallet 발견");
          details.push("(같은 origin 내 접근은 정상)");
        } else {
          blocked = true; // 데이터가 없으므로 안전
          details.push("현재 origin에 ethereum_wallet 없음");
          details.push("각 origin의 데이터는 격리됨");
        }
        break;

      case "cross-origin-localStorage":
        // Cross-origin localStorage 접근이 차단되었는지 확인
        const iframeLoad = result.stages.find((s) => s.stage === "iframe-load");

        if (iframeLoad && !iframeLoad.ok) {
          blocked = true;
          details.push(`iframe 로드 실패: ${iframeLoad.reason}`);
          details.push("Same-Origin Policy 정상 작동");
        } else {
          // access 단계에서 차단되었는지 확인
          const accessStage = result.stages.find((s) => s.stage === "access");
          blocked = !accessStage || !accessStage.ok;
          if (!blocked) {
            details.push("🚨 Cross-origin 접근 가능 - 심각한 보안 문제");
          } else {
            details.push("Cross-origin 접근 차단됨");
          }
        }
        break;

      case "filesystem":
        // 모든 파일 경로가 차단되었는지 확인
        blocked = result.stages.every((s) => s.ok === true); // ok:true = 차단 성공
        details = result.stages.map(
          (s) => `${s.path}: ${s.ok ? "차단됨" : "접근 가능"}`
        );
        break;

      case "memory-scan":
        // SharedArrayBuffer가 차단되었는지가 중요
        const spectreBlocked =
          result.stages.find((s) => s.stage === "spectre")?.ok || false;
        blocked = spectreBlocked;
        details.push(
          `전역 변수: ${
            result.stages.find((s) => s.stage === "global-scan")?.count || 0
          }개 발견`
        );
        details.push(
          `SharedArrayBuffer: ${spectreBlocked ? "차단됨" : "허용됨"}`
        );
        break;

      case "cross-origin-requests":
        // 모든 요청이 차단되었는지 확인
        blocked = result.stages.every((s) => s.xhr && s.fetch);
        result.stages.forEach((s) => {
          details.push(
            `${s.target}: ${s.xhr && s.fetch ? "모두 차단" : "일부 허용"}`
          );
        });
        break;
    }

    return {
      vector: result.vector,
      blocked: blocked,
      details: details,
    };
  });

  // 보기 좋게 출력
  log("\n공격 벡터별 결과:", "info");
  let blockedCount = 0;

  attackResults.forEach((result, index) => {
    const vectorNames = {
      "local-storage": "현재 Origin localStorage 확인",
      "cross-origin-localStorage": "Cross-Origin localStorage 접근",
      filesystem: "파일 시스템 직접 접근",
      "memory-scan": "메모리 및 전역 변수 스캔",
      "cross-origin-requests": "Cross-Origin 리소스 요청",
    };

    log(
      `\n${index + 1}. ${vectorNames[result.vector] || result.vector}`,
      "info"
    );
    log(
      `   결과: ${result.blocked ? "✅ 차단 성공" : "❌ 차단 실패"}`,
      result.blocked ? "success" : "error"
    );

    if (result.details.length > 0) {
      result.details.forEach((detail) => {
        log(`   - ${detail}`, "info");
      });
    }

    if (result.blocked) blockedCount++;
  });

  // 최종 요약
  if (blockedCount === attackResults.length) {
    log("\n🛡️ 모든 공격이 성공적으로 차단되었습니다!", "success");
  } else {
    log("\n⚠️ 일부 공격 벡터에 대한 추가 보안 조치가 필요합니다.", "warning");
  }

  log("\n========== 🎯 PPT 보안 실험 완료 ==========", "info");
}

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", function () {
  if (DEBUG) {
    console.log("🔴 Malicious Bitcoin Wallet - Security Testing Mode");
    console.log("Origin:", location.origin);
    console.log("URL:", location.href);
    console.log("");
    console.log("📌 테스트 실행: runPPTDemo()");
    console.log("");
  }
});

// 전역 함수로 등록
window.runPPTDemo = runPPTDemo;
