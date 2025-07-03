// Solana 어댑터 구현
class SolanaAdapter extends CoinAdapter {
  constructor(config) {
    super(config);
    this.connection = null;
    this.solanaWeb3 = window.solanaWeb3;
  }

  // RPC 연결 초기화
  async initProvider() {
    if (!this.connection) {
      this.connection = new this.solanaWeb3.Connection(
        this.config.network.rpcEndpoint,
        'confirmed'
      );
    }
    return this.connection;
  }

  // 새 지갑 생성
  async generateWallet() {
    // 니모닉 생성
    const mnemonic = this.solanaWeb3.generateMnemonic();
    
    // 니모닉으로부터 키페어 생성
    const keypair = this.solanaWeb3.keypairFromMnemonic(mnemonic);
    
    return {
      address: keypair.publicKey.toString(),
      privateKey: Array.from(keypair.secretKey, byte => byte.toString(16).padStart(2, '0')).join(''),
      mnemonic: mnemonic,
      publicKey: keypair.publicKey.toString()
    };
  }

  // 니모닉으로 지갑 복구
  async importFromMnemonic(mnemonic) {
    try {
      // 니모닉 유효성 검사
      if (!this.solanaWeb3.bip39.validateMnemonic(mnemonic)) {
        throw new Error('유효하지 않은 니모닉입니다');
      }
      
      // 니모닉으로부터 키페어 복구
      const keypair = this.solanaWeb3.keypairFromMnemonic(mnemonic);
      
      return {
        address: keypair.publicKey.toString(),
        privateKey: Array.from(keypair.secretKey, byte => byte.toString(16).padStart(2, '0')).join(''),
        mnemonic: mnemonic,
        publicKey: keypair.publicKey.toString()
      };
    } catch (error) {
      throw new Error(error.message || '니모닉 복구에 실패했습니다');
    }
  }

  // 개인키로 지갑 가져오기
  async importFromPrivateKey(privateKey) {
    try {
      // Hex string을 Uint8Array로 변환
      const secretKey = Uint8Array.from(
        privateKey.match(/.{1,2}/g).map(byte => parseInt(byte, 16))
      );
      
      const keypair = this.solanaWeb3.Keypair.fromSecretKey(secretKey);
      
      return {
        address: keypair.publicKey.toString(),
        privateKey: privateKey,
        publicKey: keypair.publicKey.toString()
      };
    } catch (error) {
      throw new Error('유효하지 않은 개인키입니다');
    }
  }

  // 잔액 조회
  async getBalance(address) {
    try {
      await this.initProvider();
      const publicKey = new this.solanaWeb3.PublicKey(address);
      const balance = await this.connection.getBalance(publicKey);
      return balance.toString();
    } catch (error) {
      console.error('잔액 조회 실패:', error);
      return "0";
    }
  }

  // 잔액 포맷팅 (lamports to SOL)
  formatBalance(balance) {
    const sol = parseInt(balance) / this.solanaWeb3.LAMPORTS_PER_SOL;
    return sol.toFixed(4);
  }

  // 주소 축약
  shortenAddress(address) {
    if (!address || address.length < 8) return address;
    return `${address.slice(0, 4)}...${address.slice(-4)}`;
  }

  // 주소 유효성 검사
  isValidAddress(address) {
    try {
      new this.solanaWeb3.PublicKey(address);
      return true;
    } catch (error) {
      return false;
    }
  }

  // 트랜잭션 전송
  async sendTransaction(params) {
    const { to, amount, privateKey } = params;
    
    if (!this.isValidAddress(to)) {
      throw new Error('유효하지 않은 주소입니다');
    }

    try {
      await this.initProvider();
      
      // 개인키로 키페어 복구
      const secretKey = Uint8Array.from(
        privateKey.match(/.{1,2}/g).map(byte => parseInt(byte, 16))
      );
      const fromKeypair = this.solanaWeb3.Keypair.fromSecretKey(secretKey);
      
      // 전송할 금액 계산
      const lamportsToSend = Math.floor(parseFloat(amount) * this.solanaWeb3.LAMPORTS_PER_SOL);
      
      // 수신자 계정 정보 확인
      const toPublicKey = new this.solanaWeb3.PublicKey(to);
      const receiverInfo = await this.connection.getAccountInfo(toPublicKey);
      
      // Rent-exempt minimum (약 0.00203928 SOL = 2039280 lamports)
      const rentExemptMinimum = await this.connection.getMinimumBalanceForRentExemption(0);
      
      // 수신자가 새 계정인 경우
      if (!receiverInfo) {
        if (lamportsToSend < rentExemptMinimum) {
          const minSOL = (rentExemptMinimum / this.solanaWeb3.LAMPORTS_PER_SOL).toFixed(9);
          throw new Error(`새 계정에는 최소 ${minSOL} SOL을 보내야 합니다`);
        }
      }
      
      // 송신자 잔액 확인
      const senderBalance = await this.connection.getBalance(fromKeypair.publicKey);
      const transactionFee = 5000; // 기본 트랜잭션 수수료
      
      // 송신자가 충분한 잔액을 가지고 있는지 확인
      const totalRequired = lamportsToSend + transactionFee;
      if (senderBalance < totalRequired) {
        const currentSOL = (senderBalance / this.solanaWeb3.LAMPORTS_PER_SOL).toFixed(9);
        const requiredSOL = (totalRequired / this.solanaWeb3.LAMPORTS_PER_SOL).toFixed(9);
        throw new Error(`잔액이 부족합니다. 현재: ${currentSOL} SOL, 필요: ${requiredSOL} SOL`);
      }
      
      // 송신자가 rent-exempt minimum을 유지할 수 있는지 확인
      const senderRemainingBalance = senderBalance - totalRequired;
      if (senderRemainingBalance < rentExemptMinimum) {
        const maxSendable = Math.max(0, senderBalance - rentExemptMinimum - transactionFee);
        const maxSendableSOL = (maxSendable / this.solanaWeb3.LAMPORTS_PER_SOL).toFixed(9);
        throw new Error(`송신 계정에도 최소 잔액을 유지해야 합니다. 최대 전송 가능: ${maxSendableSOL} SOL`);
      }
      
      // 트랜잭션 생성
      const transaction = new this.solanaWeb3.Transaction().add(
        this.solanaWeb3.SystemProgram.transfer({
          fromPubkey: fromKeypair.publicKey,
          toPubkey: new this.solanaWeb3.PublicKey(to),
          lamports: lamportsToSend
        })
      );
      
      // 트랜잭션 전송
      const signature = await this.solanaWeb3.sendAndConfirmTransaction(
        this.connection,
        transaction,
        [fromKeypair]
      );
      
      console.log('Solana 트랜잭션 전송 완료:', signature);
      
      return {
        hash: signature,
        signature: signature,
      };
    } catch (error) {
      console.error('트랜잭션 전송 실패:', error);
      
      // rent 관련 오류 메시지 개선
      if (error.message && error.message.includes('insufficient funds for rent')) {
        throw new Error('계정에 최소 잔액(0.00203928 SOL)을 유지해야 합니다');
      }
      
      throw new Error(error.message || '트랜잭션 전송에 실패했습니다');
    }
  }

  // 트랜잭션 상태 조회
  async getTransactionStatus(txHash) {
    try {
      await this.initProvider();
      const status = await this.connection.getSignatureStatus(txHash);
      
      if (!status || !status.value) {
        return {
          status: 'pending',
          confirmations: 0
        };
      }
      
      return {
        status: status.value.err ? 'failed' : 'confirmed',
        confirmations: status.value.confirmations || 0
      };
    } catch (error) {
      console.error('트랜잭션 상태 조회 실패:', error);
      return {
        status: 'unknown',
        confirmations: 0
      };
    }
  }

  // 블록 번호 조회 (Solana는 slot 사용)
  async getBlockNumber() {
    try {
      await this.initProvider();
      const slot = await this.connection.getSlot();
      return slot;
    } catch (error) {
      console.error('Slot 조회 실패:', error);
      return 0;
    }
  }

  // 수수료 조회 (Solana는 고정 수수료 사용)
  async getGasPrice() {
    try {
      await this.initProvider();
      
      // Solana는 고정 수수료를 사용하며, 우선순위 수수료를 추가할 수 있음
      // 기본 수수료는 5000 lamports (0.000005 SOL)
      const baseFee = 5000;
      
      return {
        low: baseFee.toString(),
        medium: (baseFee * 2).toString(),  // 2배
        high: (baseFee * 5).toString()     // 5배 (우선순위 높음)
      };
    } catch (error) {
      console.error('수수료 조회 실패:', error);
      // 기본값 반환
      return {
        low: "5000",
        medium: "10000",
        high: "25000"
      };
    }
  }

  // 트랜잭션 수수료 예상
  async estimateFee(txParams) {
    try {
      // Solana의 기본 트랜잭션 수수료는 5000 lamports
      return "5000";
    } catch (error) {
      console.error('수수료 예상 실패:', error);
      return "5000";
    }
  }

}

// 어댑터 내보내기
if (typeof module !== 'undefined' && module.exports) {
  module.exports = SolanaAdapter;
} else {
  window.SolanaAdapter = SolanaAdapter;
}