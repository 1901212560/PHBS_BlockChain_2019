import java.util.*;
public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool utxoPool;
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        double sumoutput = 0;
        double suminput = 0;
        UTXOPool uniquePool = new UTXOPool();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            Transaction.Output out = utxoPool.getTxOutput(utxo);
            if (out == null)    return false;  //check(1)
            if (!Crypto.verifySignature(out.address, tx.getRawDataToSign(i), in.signature)) {
                return false;         //check(2)
            }
            if (uniquePool.contains(utxo)) {
                return false;       //check(3)
            }
            uniquePool.addUTXO(utxo, out);
            if (out.value < 0)  return false;
            suminput += out.value;
        }
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) {
                return false;       //check(4)
            }
            sumoutput += output.value;
        }
        return suminput >= sumoutput;   //check(5)
}


    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        //stop the algorithm until no new transaction is valid
        HashSet<Transaction>txvalid = new HashSet<>();
        while (true) {
            boolean updateflag = false;
            for(Transaction tx: possibleTxs){
                if(txvalid.contains(tx)) continue;
                if(isValidTx(tx)){
                    txvalid.add(tx);
                    updateflag = true;
                    //add unspent coin to UTXOpool
                    for(int i=0 ; i<tx.numOutputs() ; i++){
                        UTXO utxo = new UTXO(tx.getHash(),i);
                        utxoPool.addUTXO(utxo, tx.getOutput(i));
                    }
                    //delete spent coin from UTXOpool
                    for(int i=0 ; i<tx.numInputs() ; i++){
                        Transaction.Input input = tx.getInput(i);
                        UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
                        utxoPool.removeUTXO(utxo);
                    }
                }
            }
            if(!updateflag)break;
        };
        Transaction[] resulttx = new Transaction[txvalid.size()];
        int j =0;
        for(Transaction tx : txvalid)
            resulttx[j++] = tx;
        return resulttx;
    }


}
