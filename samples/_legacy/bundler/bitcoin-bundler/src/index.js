// BitcoinJS-lib 번들링을 위한 엔트리 파일
import * as bitcoin from 'bitcoinjs-lib';
import * as bip39 from 'bip39';
import { BIP32Factory } from 'bip32';
import { ECPairFactory } from 'ecpair';
import ecc from '@bitcoinerlab/secp256k1';
import { Buffer } from 'buffer';

// ECC 라이브러리로 BIP32와 ECPair 팩토리 생성
const bip32 = BIP32Factory(ecc);
const ECPair = ECPairFactory(ecc);

// @bitcoinerlab/secp256k1을 bitcoinjs-lib에 연결
bitcoin.initEccLib(ecc);

// 전역 Buffer 설정 (브라우저 호환성)
if (typeof window !== 'undefined') {
  window.Buffer = Buffer;
}

// BitcoinJS 객체 생성
const BitcoinJS = {
  ...bitcoin,
  
  // BIP32와 ECPair 추가
  bip32,
  ECPair,
  
  // BIP39 추가
  bip39,
  
  // 헬퍼 함수들 추가
  hdWalletFromMnemonic: (mnemonic, network = bitcoin.networks.bitcoin) => {
    // 니모닉 → 시드
    const seed = bip39.mnemonicToSeedSync(mnemonic);
    
    // 시드 → HD 지갑
    return bip32.fromSeed(seed, network);
  },
  
  generateAddress: (hdWallet, index = 0, network = bitcoin.networks.bitcoin) => {
    // BIP84 경로: m/84'/0'/0'/0/index (메인넷)
    // BIP84 경로: m/84'/1'/0'/0/index (테스트넷)
    const coinType = network === bitcoin.networks.testnet ? 1 : 0;
    const path = `m/84'/${coinType}'/0'/0/${index}`;
    const child = hdWallet.derivePath(path);
    
    // P2WPKH 주소 생성 (bc1...)
    const { address } = bitcoin.payments.p2wpkh({ 
      pubkey: child.publicKey, 
      network 
    });
    
    return {
      address,
      privateKey: child.toWIF(),
      publicKey: child.publicKey
    };
  },
  
  generateMnemonic: (strength = 128) => bip39.generateMnemonic(strength),
  validateMnemonic: (mnemonic) => bip39.validateMnemonic(mnemonic),
  
  // Buffer 추가
  Buffer
};

// 전역 변수로도 노출 (미니앱에서 사용하기 위해)
if (typeof window !== 'undefined') {
  window.BitcoinJS = BitcoinJS;
  
}

// UMD를 위한 default export
export default BitcoinJS;