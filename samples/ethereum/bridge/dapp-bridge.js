(function() {
    'use strict';
    
    // 중복 설치 방지
    if (window.__anamEthereumBridgeV2Installed) {
        console.log('[AnamWallet v2.0] Ethereum bridge already installed');
        return;
    }
    window.__anamEthereumBridgeV2Installed = true;
    
    console.log('[AnamWallet v2.0] Installing Ethereum DApp bridge');
    
    // ================================================
    // v2.0: WalletBridge를 JavaScript에서 직접 구현
    // Native는 WalletNative 인터페이스만 제공
    // ================================================
    
    // Step 1: WalletBridge 구현
    window.WalletBridge = {
        _callbacks: {},
        _timeout: 30000,  // Ethereum은 30초 타임아웃
        
        request: function(requestId, payload) {
            console.log('[WalletBridge] Request:', requestId, payload);
            
            return new Promise((resolve, reject) => {
                this._callbacks[requestId] = { resolve, reject };
                
                // Native 호출 (v2.0: WalletNative만 사용)
                if (!window.WalletNative) {
                    console.error('[WalletBridge] WalletNative not found');
                    reject(new Error('WalletNative interface not found'));
                    delete this._callbacks[requestId];
                    return;
                }
                
                try {
                    window.WalletNative.universalBridge(requestId, payload);
                } catch (e) {
                    console.error('[WalletBridge] Native call failed:', e);
                    reject(e);
                    delete this._callbacks[requestId];
                    return;
                }
                
                // 타임아웃 설정 (30초)
                setTimeout(() => {
                    if (this._callbacks[requestId]) {
                        console.warn('[WalletBridge] Request timeout:', requestId);
                        reject({
                            code: -32000,
                            message: 'Request timeout after 30 seconds'
                        });
                        delete this._callbacks[requestId];
                    }
                }, this._timeout);
            });
        },
        
        handleResponse: function(requestId, response) {
            console.log('[WalletBridge] Response:', requestId, response);
            
            const callback = this._callbacks[requestId];
            if (!callback) {
                console.warn('[WalletBridge] No callback for:', requestId);
                return;
            }
            
            try {
                // response는 이미 파싱된 객체
                if (response.error) {
                    callback.reject(response.error);
                } else {
                    callback.resolve(response);
                }
            } catch (e) {
                console.error('[WalletBridge] Response handling error:', e);
                callback.reject(e);
            }
            
            delete this._callbacks[requestId];
        }
    };
    
    console.log('[AnamWallet v2.0] WalletBridge ready');
    
    // ================================================
    // Ethereum Provider 구현 (EIP-1193)
    // ================================================
    
    // EIP-1193 Provider 구현
    const provider = {
        _isAnamWallet: true,
        isAnamWallet: true,
        isMetaMask: false, // MetaMask 전용 코드 경로 회피
        _isConnected: false,  // 연결 상태 추적
        isConnected: function() { 
            const connected = this._isConnected && this._accounts.length > 0;
            console.log('[Ethereum] isConnected() called ->', connected, '(_isConnected:', this._isConnected, 'accounts:', this._accounts.length, ')');
            return connected;
        },
        
        // 상태 관리
        _accounts: [],
        selectedAddress: null,
        chainId: '0xaa36a7', // Sepolia testnet
        networkVersion: '11155111',
        _permissions: [],
        
        // 이벤트 관리
        _events: {},
        
        // 중복 요청 방지를 위한 pending 요청 추적
        _pendingRequests: new Map(),
        
        // 메인 request 메서드
        request: async function({ method, params }) {
            console.log(`[Ethereum] ${method}`, params);
            
            // 캐시된 데이터 즉시 반환 (네트워크 요청 없이)
            if (method === 'eth_chainId') {
                console.log('[Ethereum] eth_chainId (cached) ->', this.chainId);
                return this.chainId;
            }
            
            if (method === 'eth_accounts') {
                console.log('[Ethereum] eth_accounts (cached) ->', this._accounts);
                return this._accounts || [];
            }
            
            if (method === 'wallet_getPermissions') {
                console.log('[Ethereum] wallet_getPermissions (cached) ->', this._permissions);
                return this._permissions || [];
            }
            
            // eth_requestAccounts 중복 요청 방지
            if (method === 'eth_requestAccounts') {
                const pendingRequest = this._pendingRequests.get(method);
                if (pendingRequest) {
                    console.log('[Ethereum] Returning pending eth_requestAccounts request');
                    return pendingRequest;
                }
            }
            
            const requestId = `eth_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
            
            const payload = {
                type: 'ethereum_rpc',
                method: method,
                params: params || []
            };
            
            try {
                // v2.0: WalletBridge 사용
                const requestPromise = window.WalletBridge.request(
                    requestId,
                    JSON.stringify(payload)
                ).then(rpc => {
                    // Android에서 파싱된 객체를 받음: { jsonrpc, id, result | error }
                    console.log(`[Ethereum] RPC response for ${method}:`, rpc);
                    console.log(`[Ethereum] RPC response type:`, typeof rpc);
                    console.log(`[Ethereum] RPC response keys:`, Object.keys(rpc || {}));
                    
                    // RPC 에러 처리
                    if (rpc.error) {
                        console.error(`[Ethereum] RPC Error:`, rpc.error);
                        throw rpc.error;
                    }
                    
                    // result 추출
                    const result = rpc.result;
                    console.log(`[Ethereum] Extracted result:`, result);
                    
                    // 특정 메서드에 대한 처리 및 상태 업데이트
                    if (method === 'eth_requestAccounts') {
                        console.log('[Ethereum] eth_requestAccounts result:', result, 'type:', typeof result, 'isArray:', Array.isArray(result));
                        
                        if (result && Array.isArray(result) && result.length > 0) {
                            this._accounts = result;
                            this.selectedAddress = result[0];
                            
                            // 처음 연결될 때 connect 이벤트 발생
                            if (!this._isConnected) {
                                this._isConnected = true;
                                this._emit('connect', { chainId: this.chainId });
                            }
                            
                            this._emit('accountsChanged', result);
                            
                            // 디버깅: 연결 상태 로그
                            console.log('[AnamWallet v2.0] Connection established:');
                            console.log('  returning eth_requestAccounts:', result);
                            console.log('  chainId (hex):', this.chainId);
                            console.log('  eth_accounts:', this._accounts);
                            console.log('  selectedAddress:', this.selectedAddress);
                            console.log('  isConnected():', this.isConnected());
                            console.log('  _isConnected:', this._isConnected);
                        } else {
                            console.log('[Ethereum] eth_requestAccounts failed conditions:', {
                                hasResult: !!result,
                                isArray: Array.isArray(result),
                                length: result ? result.length : 0
                            });
                        }
                    } else if (method === 'wallet_switchEthereumChain') {
                        // 표준: 성공 시 null 반환하고 chainChanged 이벤트 발생
                        this._emit('chainChanged', this.chainId);
                        return null; // 표준 동작: 체인 전환 성공 시 null 반환
                    } else if (method === 'wallet_requestPermissions') {
                        // 권한 캐싱
                        this._permissions = Array.isArray(result) ? result : [];
                    }
                    
                    // 반환값 정리
                    console.log(`[Ethereum] Returning result for ${method}:`, result);
                    return result;
                }).finally(() => {
                    // 요청 완료 후 pending 목록에서 제거
                    this._pendingRequests.delete(method);
                });
                
                // eth_requestAccounts는 pending 목록에 추가
                if (method === 'eth_requestAccounts') {
                    this._pendingRequests.set(method, requestPromise);
                }
                
                const finalResult = await requestPromise;
                console.log(`[Ethereum] Final result for ${method}:`, finalResult);
                return finalResult;
            } catch (error) {
                console.error(`[Ethereum] Error in ${method}:`, error);
                // 에러 발생 시에도 pending 목록에서 제거
                this._pendingRequests.delete(method);
                throw error;
            }
        },
        
        // 이벤트 메서드
        on: function(eventName, handler) {
            if (!this._events[eventName]) {
                this._events[eventName] = [];
            }
            this._events[eventName].push(handler);
            return this;
        },
        
        removeListener: function(eventName, handler) {
            if (!this._events[eventName]) return this;
            this._events[eventName] = this._events[eventName].filter(h => h !== handler);
            return this;
        },
        
        // 일부 DApp은 off 메서드를 사용
        off: function(eventName, handler) {
            return this.removeListener(eventName, handler);
        },
        
        _emit: function(eventName, ...args) {
            console.log(`[Ethereum] Emitting event '${eventName}':`, args);
            if (!this._events[eventName]) {
                console.log(`[Ethereum] No handlers for event '${eventName}'`);
                return;
            }
            console.log(`[Ethereum] Found ${this._events[eventName].length} handlers for '${eventName}'`);
            this._events[eventName].forEach(handler => {
                try {
                    handler(...args);
                } catch (err) {
                    console.error(`[Ethereum] Event handler error:`, err);
                }
            });
        },
        
        // 연결 해제 지원
        disconnect: function() {
            this._accounts = [];
            this.selectedAddress = null;
            this._isConnected = false;
            this._emit('accountsChanged', []);
            this._emit('disconnect', { code: 4900, message: 'User disconnected' });
            return Promise.resolve(null);
        },
        
        // 레거시 호환성 메서드
        sendAsync: function(payload, cb) {
            this.request(payload).then(
                (result) => cb(null, { id: payload.id, jsonrpc: '2.0', result }),
                (err) => cb(err, null)
            );
        },
        
        send: function(methodOrPayload, params) {
            if (typeof methodOrPayload === 'string') {
                return this.request({ method: methodOrPayload, params });
            }
            return this.request(methodOrPayload);
        }
    };
    
    // window.ethereum 설정 (fallback 지원)
    if (!window.ethereum) {
        window.ethereum = provider;
        console.log('[AnamWallet v2.0] window.ethereum installed');
        // 일부 DApp은 이 이벤트를 기다림
        window.dispatchEvent(new Event('ethereum#initialized'));
        console.log('[AnamWallet v2.0] ethereum#initialized event dispatched');
    } else {
        console.log('[AnamWallet v2.0] window.ethereum already exists, skipping installation');
    }
    
    // ================================================
    // EIP-6963: 다중 지갑 발견 표준
    // Native는 이 표준의 존재를 모르지만,
    // JavaScript가 알아서 처리합니다.
    // ================================================
    
    // 안정적인 UUID 생성 (페이지 새로고침 시에도 동일한 값 유지)
    const getStableUuid = () => {
        const key = 'anam_wallet_uuid';
        let uuid = localStorage.getItem(key);
        if (!uuid) {
            uuid = crypto.randomUUID();
            localStorage.setItem(key, uuid);
        }
        return uuid;
    };
    
    const info = {
        uuid: getStableUuid(),
        name: 'Anam Wallet',
        icon: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzIiIGhlaWdodD0iMzIiIHZpZXdCb3g9IjAgMCAzMiAzMiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjMyIiBoZWlnaHQ9IjMyIiByeD0iOCIgZmlsbD0iIzYzNjZGMSIvPgo8cGF0aCBkPSJNMTYgOEwxMCAxNkwxNiAyNEwyMiAxNkwxNiA4WiIgZmlsbD0id2hpdGUiLz4KPC9zdmc+',
        rdns: 'com.anam145.wallet'
    };
    
    // Provider 발표 함수
    function announceProvider() {
        window.dispatchEvent(new CustomEvent('eip6963:announceProvider', {
            detail: Object.freeze({ info, provider })
        }));
    }
    
    // 초기 발표
    announceProvider();
    
    // DApp이 나중에 요청할 때도 다시 발표
    window.addEventListener('eip6963:requestProvider', announceProvider);
    
    // enable 메서드 추가 (레거시 호환성)
    provider.enable = () => provider.request({ method: 'eth_requestAccounts' });
    
    console.log('[AnamWallet v2.0] Ethereum bridge ready');
})();
