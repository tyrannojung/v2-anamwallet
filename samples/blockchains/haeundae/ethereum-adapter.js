// Ethereum Adapter - CoinAdapter 구현
// ethers.js를 사용하여 이더리움 기능 구현

class EthereumAdapter extends CoinAdapter {
  constructor(config) {
    super(config);
    this.provider = null;
  }

  // Provider 초기화
  async initProvider() {
    if (!this.provider && typeof ethers !== 'undefined') {
      this.provider = new ethers.providers.JsonRpcProvider(this.config.network.rpcEndpoint);
    }
    return this.provider;
  }

  /* ================================================================
   * 1. 지갑 생성 및 관리
   * ================================================================ */
  
  async generateWallet() {
    // ethers.js를 사용하여 새 지갑 생성
    const wallet = ethers.Wallet.createRandom();
    
    return {
      address: wallet.address,
      privateKey: wallet.privateKey,
      mnemonic: wallet.mnemonic.phrase
    };
  }

  async importFromMnemonic(mnemonic) {
    try {
      const wallet = ethers.Wallet.fromMnemonic(mnemonic);
      return {
        address: wallet.address,
        privateKey: wallet.privateKey
      };
    } catch (error) {
      throw new Error("유효하지 않은 니모닉입니다: " + error.message);
    }
  }

  async importFromPrivateKey(privateKey) {
    try {
      const wallet = new ethers.Wallet(privateKey);
      return {
        address: wallet.address
      };
    } catch (error) {
      throw new Error("유효하지 않은 개인키입니다: " + error.message);
    }
  }

  isValidAddress(address) {
    return ethers.utils.isAddress(address);
  }

  /* ================================================================
   * 2. 잔액 조회
   * ================================================================ */
  
  async getBalance(address) {
    await this.initProvider();
    const balance = await this.provider.getBalance(address);
    return balance.toString(); // Wei 단위 BigNumber를 문자열로
  }

  formatBalance(balance) {
    // Wei를 ETH로 변환
    return ethers.utils.formatEther(balance);
  }

  /* ================================================================
   * 3. 트랜잭션 처리
   * ================================================================ */
  
  async sendTransaction(params) {
    await this.initProvider();
    
    const {
      from,
      to,
      amount,
      privateKey,
      gasPrice: customGasPrice,
      gasLimit = 21000,
      data = '0x',
      nonce
    } = params;

    try {
      // 지갑 생성
      const wallet = new ethers.Wallet(privateKey, this.provider);
      
      // 트랜잭션 객체 구성
      const tx = {
        to: to,
        value: ethers.utils.parseEther(amount),
        gasLimit: gasLimit,
        data: data
      };

      // 가스비 설정
      if (customGasPrice) {
        tx.gasPrice = ethers.utils.parseUnits(customGasPrice, 'gwei');
      } else {
        const gasPrice = await this.provider.getGasPrice();
        tx.gasPrice = gasPrice;
      }

      // nonce 설정 (옵션)
      if (nonce !== undefined) {
        tx.nonce = nonce;
      }

      // 트랜잭션 전송
      const transaction = await wallet.sendTransaction(tx);
      
      // 트랜잭션 해시 반환
      return {
        hash: transaction.hash,
        gasPrice: tx.gasPrice.toString(),
        gasLimit: tx.gasLimit.toString()
      };
    } catch (error) {
      throw new Error("트랜잭션 전송 실패: " + error.message);
    }
  }

  async getTransactionStatus(txHash) {
    await this.initProvider();
    
    try {
      const receipt = await this.provider.getTransactionReceipt(txHash);
      
      if (!receipt) {
        return {
          status: 'pending',
          confirmations: 0
        };
      }

      const currentBlock = await this.provider.getBlockNumber();
      const confirmations = currentBlock - receipt.blockNumber;

      return {
        status: receipt.status === 1 ? 'success' : 'failed',
        confirmations: confirmations,
        blockNumber: receipt.blockNumber,
        gasUsed: receipt.gasUsed.toString()
      };
    } catch (error) {
      throw new Error("트랜잭션 상태 조회 실패: " + error.message);
    }
  }

  /* ================================================================
   * 4. 수수료 관련
   * ================================================================ */
  
  async getGasPrice() {
    await this.initProvider();
    
    try {
      const gasPrice = await this.provider.getGasPrice();
      const gasPriceGwei = ethers.utils.formatUnits(gasPrice, 'gwei');
      
      // 빠름, 보통, 느림 옵션 제공
      return {
        low: (parseFloat(gasPriceGwei) * 0.8).toFixed(2),
        medium: gasPriceGwei,
        high: (parseFloat(gasPriceGwei) * 1.2).toFixed(2)
      };
    } catch (error) {
      throw new Error("가스비 조회 실패: " + error.message);
    }
  }

  async estimateFee(txParams) {
    await this.initProvider();
    
    try {
      const { to, amount, data = '0x' } = txParams;
      
      // 가스 한도 예측
      const estimatedGas = await this.provider.estimateGas({
        to: to,
        value: ethers.utils.parseEther(amount),
        data: data
      });
      
      // 현재 가스 가격
      const gasPrice = await this.provider.getGasPrice();
      
      // 예상 수수료 계산 (가스 한도 * 가스 가격)
      const fee = estimatedGas.mul(gasPrice);
      
      return ethers.utils.formatEther(fee);
    } catch (error) {
      // 예측 실패 시 기본값 반환
      console.error("수수료 예측 실패:", error);
      return "0.001"; // 기본 수수료
    }
  }

  /* ================================================================
   * 5. 이더리움 특화 기능
   * ================================================================ */
  
  // 현재 블록 번호 조회
  async getBlockNumber() {
    await this.initProvider();
    return await this.provider.getBlockNumber();
  }

  // 네트워크 정보 조회
  async getNetwork() {
    await this.initProvider();
    return await this.provider.getNetwork();
  }

  // ENS 이름 해석
  async resolveENS(ensName) {
    await this.initProvider();
    try {
      const address = await this.provider.resolveName(ensName);
      return address;
    } catch (error) {
      return null;
    }
  }
}

// 내보내기
if (typeof module !== 'undefined' && module.exports) {
  module.exports = EthereumAdapter;
} else {
  window.EthereumAdapter = EthereumAdapter;
}