// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.


import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    //create a class to include more related information about block in a block chain
    private class BlockNode {
        public Block block;
        public BlockNode parent;
        public ArrayList<BlockNode> children;
        public int height;
        private UTXOPool uPool;

        public BlockNode(Block block, BlockNode parent, UTXOPool uPool) {
            this.block = block;
            this.parent = parent;
            children = new ArrayList<BlockNode>();
            this.uPool = uPool;
            if (parent != null) {
                height = parent.height + 1;
                parent.children.add(this);
            } else {
                height = 1;    //no block in front, genesis block
            }

        }
        public UTXOPool getUTXOPoolCopy() {
            return new UTXOPool(uPool);
        }

    }

    private HashMap<ByteArrayWrapper, BlockNode> blockChainmap;  //the map between hash and blocknode
    private int height;               // height of blockChain
    private BlockNode maxHeightNode;  // top block node in current blockChain.
    private TransactionPool txPool;   // the transactions not processed in current blockChain

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS

        blockChainmap = new HashMap<ByteArrayWrapper, BlockNode>(); // create an empty blockchain
        txPool = new TransactionPool();  // create an empty transaction pool
        UTXOPool utxoPool = new UTXOPool();  // create an empty UTXO pool

        // add the coinbase transaction to utxo pool
        Transaction coinbase = genesisBlock.getCoinbase();
        UTXO utxoCoinbase = new UTXO(coinbase.getHash(), 0);
        utxoPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));

        // append the genesis block to block chain
        BlockNode genesis = new BlockNode(genesisBlock, null, utxoPool);  //utxopool: only coinbase
        blockChainmap.put(new ByteArrayWrapper(genesisBlock.getHash()), genesis);  //put(key,value)
        height = 1;

        // set maxHeightNode
        maxHeightNode = genesis;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return maxHeightNode.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return maxHeightNode.getUTXOPoolCopy();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        // the hash of parent node cannot be null
        byte[] parentBlockHash = block.getPrevBlockHash();
        if (parentBlockHash == null)
            return false;

        // the parent node must be exist in the block chain
        BlockNode parentBlockNode = blockChainmap.get(new ByteArrayWrapper(parentBlockHash));  //use key to get value
        if (parentBlockNode == null) {
            return false;
        }

        // check validity of transactions in the block
        UTXOPool parentUTXOPool = parentBlockNode.getUTXOPoolCopy();
        TxHandler handler = new TxHandler(parentUTXOPool);
        Transaction[] txs = block.getTransactions().toArray(new Transaction[0]);  //注意 list和 array的区别？？
        // select valid TXs and utxo pool is modified accordingly
        Transaction[] validTxs = handler.handleTxs(txs);
        if (validTxs.length != txs.length) {
            return false;    // invalid tx exists;
        }
//将coinbase放到utxopool中是不是应放在tx valid check前面？不然tx里有花了coinbase的怎么办？  答：这时候的coinbase还不能花 因为没有记账
        // add the output of the coinbase transaction to utxo pool
        UTXOPool utxoPool = handler.getUTXOPool();  //utxoes in handler have been modified
        Transaction coinbase = block.getCoinbase();
        UTXO utxoCoinbase = new UTXO(coinbase.getHash(), 0);
        utxoPool.addUTXO(utxoCoinbase, coinbase.getOutput(0));

        // Append current block to the block chain and update the height of blockChain
        BlockNode currentNode = new BlockNode(block, parentBlockNode, utxoPool);
        blockChainmap.put(new ByteArrayWrapper(block.getHash()), currentNode);
        if (currentNode.height > height) {  //如果小于怎么办  答：小于说明不是最长链，那么还是维持最长链的长度不变
            maxHeightNode = currentNode;
            height = currentNode.height;
        }
        // update the transaction pool, removing transactions used by block and return it
        ArrayList<Transaction> allTxs = block.getTransactions();
        for (Transaction tx : allTxs) {
            txPool.removeTransaction(tx.getHash());
        }

        // block should be at height > (maxHeight - CUT_OFF_AGE).
        int proposedHeight = parentBlockNode.height + 1;
        if (proposedHeight <= maxHeightNode.height - CUT_OFF_AGE) {
            return false;  //fork只能发生在最长链的最新的10个块之内
        }
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        txPool.addTransaction(tx);
    }
}