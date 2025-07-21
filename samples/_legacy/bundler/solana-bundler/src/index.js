// Solana Web3.js 번들링을 위한 엔트리 파일
import { 
  Connection, 
  Keypair, 
  PublicKey, 
  Transaction,
  SystemProgram,
  LAMPORTS_PER_SOL,
  sendAndConfirmTransaction,
  clusterApiUrl
} from '@solana/web3.js';

// 니모닉 관련 라이브러리
import * as bip39 from 'bip39';
import { derivePath } from 'ed25519-hd-key';

// 필요한 클래스와 함수들을 export
export {
  Connection,
  Keypair,
  PublicKey,
  Transaction,
  SystemProgram,
  LAMPORTS_PER_SOL,
  sendAndConfirmTransaction,
  clusterApiUrl,
  bip39,
  derivePath
};

// 전역 변수로도 노출 (미니앱에서 사용하기 위해)
if (typeof window !== 'undefined') {
  window.solanaWeb3 = {
    Connection,
    Keypair,
    PublicKey,
    Transaction,
    SystemProgram,
    LAMPORTS_PER_SOL,
    sendAndConfirmTransaction,
    clusterApiUrl,
    
    // 니모닉 관련 추가
    bip39,
    derivePath,
    
    // 헬퍼 함수: 니모닉으로부터 Keypair 생성
    keypairFromMnemonic: (mnemonic, accountIndex = 0) => {
      // 니모닉 → 시드
      const seed = bip39.mnemonicToSeedSync(mnemonic);
      
      // Solana 표준 파생 경로
      const path = `m/44'/501'/${accountIndex}'/0'`;
      const derivedSeed = derivePath(path, seed.toString('hex')).key;
      
      // 시드 → Keypair
      return Keypair.fromSeed(derivedSeed);
    },
    
    // 니모닉 생성
    generateMnemonic: () => bip39.generateMnemonic()
  };
  
  // 버전 정보 추가
  window.solanaWeb3.version = '1.95.1';
}