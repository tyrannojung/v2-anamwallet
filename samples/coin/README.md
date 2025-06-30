# Coin 지갑 템플릿

이 템플릿은 새로운 블록체인 지갑을 빠르게 개발할 수 있도록 만들어진 기본 구조입니다.

## 🚀 사용 방법

### 1. 템플릿 복사

```bash
# 예: Bitcoin 지갑 만들기
cp -r samples/coin samples/blockchains/bitcoin
```

### 2. 설정 파일 수정

`config.js` 파일을 열어 코인 정보를 수정합니다:

```javascript
const CoinConfig = {
  name: "Bitcoin",
  symbol: "BTC",
  decimals: 8,
  network: {
    rpcEndpoint: "https://your-btc-endpoint.quicknode.pro/",
    networkName: "mainnet",
  },
  theme: {
    primaryColor: "#F7931A",
    secondaryColor: "#FFA500",
    logoSymbol: "₿",
    logoText: "Bitcoin",
  },
  // ...
};
```

### 3. CoinAdapter 구현

`pages/index/` 폴더에 코인별 어댑터를 구현합니다:

```javascript
// bitcoin-adapter.js
class BitcoinAdapter extends CoinAdapter {
  async generateWallet() {
    // Bitcoin 지갑 생성 로직
  }
  
  async getBalance(address) {
    // Bitcoin 잔액 조회 로직
  }
  
  // ... 다른 메서드들
}
```

### 4. index.js에서 어댑터 초기화

```javascript
// index.js의 DOMContentLoaded 이벤트에서
adapter = new BitcoinAdapter(CoinConfig);
```

### 5. 필요한 라이브러리 추가

`index.html`의 하단에 필요한 라이브러리를 추가합니다:

```html
<!-- Bitcoin의 경우 -->
<script src="https://unpkg.com/bitcoinjs-lib@6.1.0/dist/bitcoinjs-lib.js"></script>
```

## 📁 파일 구조

```
coin/
├── manifest.json          # 앱 메타데이터
├── app.js                 # 생명주기 정의
├── app.css                # 공통 스타일
├── config.js              # 코인 설정
├── coin-adapter.js        # 추상 클래스
├── assets/
│   └── icons/
│       └── app_icon.png   # 앱 아이콘 (추가 필요)
└── pages/
    └── index/
        ├── index.html     # UI 템플릿
        ├── index.css      # 스타일
        └── index.js       # 메인 로직
```

## 🎨 커스터마이징

### 테마 색상

CSS 변수를 통해 쉽게 변경 가능:

```css
:root {
  --coin-primary: #F7931A;    /* Bitcoin 오렌지 */
  --coin-secondary: #FFA500;
}
```

### UI 컴포넌트

필요에 따라 추가/제거 가능:
- 지갑 생성/가져오기
- 잔액 표시
- Send/Receive
- 거래 내역

## 🔧 구현 체크리스트

각 코인을 구현할 때 다음 항목들을 확인하세요:

- [ ] `config.js` 수정
- [ ] CoinAdapter 구현
  - [ ] generateWallet()
  - [ ] importFromMnemonic()
  - [ ] importFromPrivateKey()
  - [ ] isValidAddress()
  - [ ] getBalance()
  - [ ] sendTransaction()
  - [ ] getTransactionHistory()
  - [ ] getGasPrice()
  - [ ] estimateFee()
- [ ] 필요한 라이브러리 추가
- [ ] 아이콘 추가 (assets/icons/app_icon.png)
- [ ] manifest.json의 app_id 수정
- [ ] 테스트

## 📚 예제 코인 구현

### EVM 체인 (Ethereum, BSC, Avalanche, Polygon)

```javascript
import { ethers } from 'ethers';

class EVMAdapter extends CoinAdapter {
  constructor(config) {
    super(config);
    this.provider = new ethers.JsonRpcProvider(config.network.rpcEndpoint);
  }
  
  async generateWallet() {
    const wallet = ethers.Wallet.createRandom();
    return {
      address: wallet.address,
      privateKey: wallet.privateKey,
      mnemonic: wallet.mnemonic.phrase
    };
  }
}
```

### UTXO 체인 (Bitcoin, Litecoin, Dogecoin)

```javascript
import * as bitcoin from 'bitcoinjs-lib';

class UTXOAdapter extends CoinAdapter {
  async generateWallet() {
    const keyPair = bitcoin.ECPair.makeRandom();
    const { address } = bitcoin.payments.p2wpkh({ 
      pubkey: keyPair.publicKey 
    });
    return {
      address,
      privateKey: keyPair.toWIF()
    };
  }
}
```

### Ed25519 체인 (Solana, Stellar, Sui)

```javascript
import { Keypair } from '@solana/web3.js';

class Ed25519Adapter extends CoinAdapter {
  async generateWallet() {
    const keypair = Keypair.generate();
    return {
      address: keypair.publicKey.toString(),
      privateKey: bs58.encode(keypair.secretKey)
    };
  }
}
```

## 🔗 QuickNode 지원 체인

1. **Bitcoin (BTC)** - UTXO, secp256k1
2. **Ethereum (ETH)** - EVM 기준 체인
3. **BNB Smart Chain (BSC)** - EVM 호환
4. **Dogecoin (DOGE)** - UTXO, 저렴한 수수료
5. **Litecoin (LTC)** - UTXO, 빠른 블록
6. **Sui (SUI)** - Move 기반, Ed25519
7. **Solana (SOL)** - 고성능, Ed25519
8. **Stellar (XLM)** - 계정 모델, Ed25519
9. **Avalanche C-Chain (AVAX)** - EVM 호환
10. **Polygon (MATIC)** - EVM 사이드체인

## 💡 팁

1. **니모닉 지원**: UTXO 체인은 BIP39, EVM은 ethers 내장 사용
2. **주소 형식**: 각 체인별 정규식을 config.js에 정의
3. **수수료**: EVM은 가스비, UTXO는 sat/byte 계산 필요
4. **테스트**: 먼저 테스트넷에서 충분히 테스트

## 📄 라이센스

이 템플릿은 자유롭게 사용하실 수 있습니다.