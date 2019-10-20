import java.security.*;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertFalse;
import java.util.Arrays;


/** 
* BlockHandler Tester. 
* 
* @author <BOLAN>
* @since <pre>10�� 15, 2019</pre> 
* @version 1.0 
*/ 
public class BlockHandlerTest {

    int maxblocknumber=20;

    KeyPair kp_A = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair kp_B = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair kp_C = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair kp_D = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    KeyPair kp_E = KeyPairGenerator.getInstance("RSA").generateKeyPair();

    public BlockHandlerTest() throws NoSuchAlgorithmException {
    }

    @Before
    public void before() throws Exception {
    }


    @After
    public void after() throws Exception {
    }

    /*test processBlock()  */
    @Test
    public void test1() throws Exception {
        //add a no transaction block to the block chain,the result we expect is true
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block block = new Block(genesisBlock.getHash(), kp_B.getPublic());
        block.finalize();
        assertTrue("Test1 Failed ", blockHandler.processBlock(block));
    }

    @Test
    public void test2() throws Exception {
        //add another genesis block, the result we expect is false
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block genesisBlock2 = new Block(null, kp_B.getPublic());
        genesisBlock2.finalize();
        assertFalse("Test2 Failed ", blockHandler.processBlock(genesisBlock2));
    }

    @Test
    public void test3() throws Exception {
        //add a block with invalid prevhash, the result we expect is false
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        byte[] hash = genesisBlock.getHash();
        byte[] hashwrong = Arrays.copyOf(hash, hash.length);
        hashwrong[0]++;  //change the first number of genesisblock hash
        Block block = new Block(hashwrong, kp_B.getPublic());
        block.finalize();

        assertFalse("Test3 Failed", blockHandler.processBlock(block));
    }

    @Test
    public void test4() throws Exception {
        //add a block with a valid transaction , the result we expect is true
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block block = new Block(genesisBlock.getHash(), kp_B.getPublic());

        Transaction TX_A_to_C = new Transaction();
        TX_A_to_C.addInput(genesisBlock.getCoinbase().getHash(), 0);
        TX_A_to_C.addOutput(Block.COINBASE, kp_C.getPublic());
        TX_A_to_C.Txsign(kp_A.getPrivate(), 0);
        TX_A_to_C.finalize();

        block.addTransaction(TX_A_to_C);
        block.finalize();

        assertTrue("Test4 Failed", blockHandler.processBlock(block));
    }

    @Test
    public void test5() throws Exception {
        //add a block with same valid transactions , the result we expect is true
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block block = new Block(genesisBlock.getHash(), kp_B.getPublic());

        Transaction TX_A_to_BC = new Transaction();
        TX_A_to_BC.addInput(genesisBlock.getCoinbase().getHash(), 0);
        TX_A_to_BC.addOutput(10, kp_B.getPublic());
        TX_A_to_BC.addOutput(15, kp_C.getPublic());
        TX_A_to_BC.Txsign(kp_A.getPrivate(), 0);
        TX_A_to_BC.finalize();

        Transaction TX_C_to_DE = new Transaction();
        TX_C_to_DE.addInput(TX_A_to_BC.getHash(), 1);
        TX_C_to_DE.addOutput(10, kp_D.getPublic());
        TX_C_to_DE.addOutput(5, kp_E.getPublic());
        TX_C_to_DE.Txsign(kp_C.getPrivate(), 0);
        TX_C_to_DE.finalize();

        Transaction TX_D_to_E = new Transaction();
        TX_D_to_E.addInput(TX_C_to_DE.getHash(), 0);
        TX_D_to_E.addOutput(10, kp_E.getPublic());
        TX_D_to_E.Txsign(kp_D.getPrivate(), 0);
        TX_D_to_E.finalize();

        block.addTransaction(TX_A_to_BC);
        block.addTransaction(TX_C_to_DE);
        block.addTransaction(TX_D_to_E);
        block.finalize();

        assertTrue("Test5 Failed", blockHandler.processBlock(block));
    }

    @Test
    public void test6() throws Exception {
        //add a block with a double spending transaction, the result we expect is false
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block block = new Block(genesisBlock.getHash(), kp_B.getPublic());

        Transaction tx_A_to_B = new Transaction();  //tx1
        tx_A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        tx_A_to_B.addOutput(25, kp_B.getPublic());
        tx_A_to_B.Txsign(kp_A.getPrivate(), 0);
        tx_A_to_B.finalize();

        Transaction tx_B_to_C = new Transaction();   //tx2
        tx_B_to_C.addInput(tx_A_to_B.getHash(), 0);
        tx_B_to_C.addOutput(25, kp_C.getPublic());
        tx_B_to_C.Txsign(kp_B.getPrivate(), 0);
        tx_B_to_C.finalize();

        Transaction tx_A_to_E = new Transaction();   //tx4
        tx_A_to_E.addInput(genesisBlock.getCoinbase().getHash(), 0);
        tx_A_to_E.addOutput(25, kp_E.getPublic());
        tx_A_to_E.Txsign(kp_A.getPrivate(), 0);
        tx_A_to_E.finalize();

        block.addTransaction(tx_A_to_B);
        block.addTransaction(tx_B_to_C);
        block.addTransaction(tx_B_to_C);
        block.addTransaction(tx_A_to_E);
        block.finalize();

        assertFalse("Test6 Failed", blockHandler.processBlock(block));
    }

    @Test
    public void test7() throws Exception {
        //add a block with some invalid transactions
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        //negative output
        Block block1 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        Transaction TX_A_to_B = new Transaction();
        TX_A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        TX_A_to_B.addOutput(-1, kp_B.getPublic());
        TX_A_to_B.Txsign(kp_A.getPrivate(), 0);
        TX_A_to_B.finalize();
        block1.addTransaction(TX_A_to_B);
        block1.finalize();
        assertFalse("Failed to process a transaction with negative output.", blockHandler.processBlock(block1));

        //wrong signature
        Block block2 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        Transaction TX_A_to_C = new Transaction();
        TX_A_to_C.addInput(genesisBlock.getCoinbase().getHash(), 0);
        TX_A_to_C.addOutput(25, kp_C.getPublic());
        TX_A_to_C.Txsign(kp_B.getPrivate(), 0);
        TX_A_to_C.finalize();
        block2.addTransaction(TX_A_to_C);
        block2.finalize();
        assertFalse("Failed to process a transaction with wrong signature.", blockHandler.processBlock(block2));

        //invalid utxo
        Block block3 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        Transaction TX_B_to_C = new Transaction();
        TX_B_to_C.addInput(TX_A_to_B.getHash(), 0);
        TX_B_to_C.addOutput(25, kp_C.getPublic());
        TX_B_to_C.Txsign(kp_B.getPrivate(), 0);
        TX_B_to_C.finalize();
        block2.addTransaction(TX_B_to_C);
        block2.finalize();
        assertFalse("Failed to process a transaction with invalid utxo.", blockHandler.processBlock(block2));

        //no sufficient coins
        Block block4 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        Transaction TX_A_to_D = new Transaction();
        TX_A_to_D.addInput(genesisBlock.getCoinbase().getHash(), 0);
        TX_A_to_D.addOutput(30, kp_B.getPublic());
        TX_A_to_D.Txsign(kp_A.getPrivate(), 0);
        TX_A_to_D.finalize();
        block4.addTransaction(TX_A_to_D);
        block4.finalize();
        assertFalse("Failed to process a transaction with no sufficient coins.", blockHandler.processBlock(block4));
    }

    @Test
    public void test8() throws Exception {
        //add multiple blocks directly on the top of the genesis block, the result we expect is true
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block1 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        block1.finalize();
        assertTrue("Test1 Failed ", blockHandler.processBlock(block1));

        Block block2 = new Block(genesisBlock.getHash(), kp_C.getPublic());
        block2.finalize();
        assertTrue("Test1 Failed ", blockHandler.processBlock(block2));
    }

    @Test
    public void test9() throws Exception {
        //process a chain of block
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_B.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block;
        Block prevBlock = genesisBlock;
        Transaction Tx;

        for (int i = 0; i < 10; i++) {
            block = new Block(prevBlock.getHash(), kp_B.getPublic());
            Tx = new Transaction();
            Tx.addInput(prevBlock.getCoinbase().getHash(), 0);
            Tx.addOutput(25, kp_C.getPublic());
            Tx.Txsign(kp_B.getPrivate(), 0);
            Tx.finalize();
            block.addTransaction(Tx);
            block.finalize();
            assertTrue("Failed to add block " + i, blockHandler.processBlock(block));
            prevBlock = block;
        }
    }

    @Test
    public void test10() throws Exception {
        //process a block with a utxo which has been claimed in the longest chain, the result we expect is true
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block1 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        Transaction TX_A_to_BC = new Transaction();
        TX_A_to_BC.addInput(genesisBlock.getCoinbase().getHash(), 0);
        TX_A_to_BC.addOutput(15, kp_B.getPublic());
        TX_A_to_BC.addOutput(10, kp_C.getPublic());
        TX_A_to_BC.Txsign(kp_A.getPrivate(), 0);
        TX_A_to_BC.finalize();
        block1.addTransaction(TX_A_to_BC);
        block1.finalize();
        blockHandler.processBlock(block1);

        Block block2 = new Block(block1.getHash(), kp_C.getPublic());
        Transaction TX_C_to_D = new Transaction();
        TX_C_to_D.addInput(TX_A_to_BC.getHash(), 1);
        TX_C_to_D.addOutput(10, kp_D.getPublic());
        TX_C_to_D.Txsign(kp_C.getPrivate(), 0);
        TX_C_to_D.finalize();
        block2.addTransaction(TX_C_to_D);
        block2.finalize();
        blockHandler.processBlock(block2);

        Block block3 = new Block(block2.getHash(), kp_D.getPublic());
        Transaction TX_B_to_D = new Transaction();
        TX_B_to_D.addInput(TX_A_to_BC.getHash(), 0);
        TX_B_to_D.addOutput(15, kp_D.getPublic());
        TX_B_to_D.Txsign(kp_B.getPrivate(), 0);
        TX_B_to_D.finalize();
        block3.addTransaction(TX_B_to_D);
        block3.finalize();
        blockHandler.processBlock(block3);

        Block block4 = new Block(block1.getHash(), kp_B.getPublic());
        Transaction TX_B_to_E = new Transaction();
        TX_B_to_E.addInput(TX_A_to_BC.getHash(), 0);
        TX_B_to_E.addOutput(15, kp_E.getPublic());
        TX_B_to_E.Txsign(kp_B.getPrivate(), 0);
        TX_B_to_E.finalize();
        block4.addTransaction(TX_B_to_E);
        block4.finalize();
        assertTrue("Fail to process block4",blockHandler.processBlock(block4));
        assertTrue("block4 does not contain TX_B_to_E",block4.getTransactions().contains(TX_B_to_E));
    }

    @Test
    public void test11() throws Exception {
        //process a block whose height > maxheight- cut off age, the result we expect is true
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_B.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block;
        Block prevBlock = genesisBlock;
        Transaction Tx;

        //height of blockchain is 11
        for (int i = 0; i < 10; i++) {
            block = new Block(prevBlock.getHash(), kp_B.getPublic());
            Tx = new Transaction();
            Tx.addInput(prevBlock.getCoinbase().getHash(), 0);
            Tx.addOutput(25, kp_C.getPublic());
            Tx.Txsign(kp_B.getPrivate(), 0);
            Tx.finalize();
            block.addTransaction(Tx);
            block.finalize();
            blockHandler.processBlock(block);
            prevBlock = block;
        }
        //height of forkblock is 2
        Block forkblock = new Block(genesisBlock.getHash(), kp_B.getPublic());
        forkblock.finalize();
        assertTrue("Fail to process forkblock",blockHandler.processBlock(forkblock));
    }

    @Test
    public void test12() throws Exception {
        //process a block whose height <= maxheight- cut off age, the result we expect is false
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_B.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block;
        Block prevBlock = genesisBlock;
        Transaction Tx;

        //height of blockchain is 12
        for (int i = 0; i < 11; i++) {
            block = new Block(prevBlock.getHash(), kp_B.getPublic());
            Tx = new Transaction();
            Tx.addInput(prevBlock.getCoinbase().getHash(), 0);
            Tx.addOutput(25, kp_C.getPublic());
            Tx.Txsign(kp_B.getPrivate(), 0);
            Tx.finalize();
            block.addTransaction(Tx);
            block.finalize();
            blockHandler.processBlock(block);
            prevBlock = block;
        }
        //height of forkblock is 2
        Block forkblock = new Block(genesisBlock.getHash(), kp_B.getPublic());
        forkblock.finalize();
        assertFalse("Fail to process forkblock",blockHandler.processBlock(forkblock));
    }
    /*test createBlock()  */
    @Test
    public void test13() throws Exception {
        //create a block when there is no transaction in transaction pool
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block createdBlock = blockHandler.createBlock(kp_B.getPublic());

        //check that the created block has been added to the chain successfully
        assertTrue("Created block is null", createdBlock != null);
        //check there is no transaction in the created block
        assertTrue("Failed empty block creation", createdBlock.getTransactions().size() == 0);
    }

    @Test
    public void test14() throws Exception {
        //create a block when there is a valid transaction in transaction pool
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Transaction Tx_A_to_B = new Transaction();
        Tx_A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        Tx_A_to_B.addOutput(25, kp_B.getPublic());
        Tx_A_to_B.Txsign(kp_A.getPrivate(), 0);
        Tx_A_to_B.finalize();
        blockHandler.processTx(Tx_A_to_B);
        assertTrue(blockChain.getTransactionPool().getTransactions().contains(Tx_A_to_B));
        Block createdBlock = blockHandler.createBlock(kp_B.getPublic());

        assertTrue("Not successfully Create a block", createdBlock != null);
        assertTrue("Number of transactions is" + createdBlock.getTransactions().size(),
                createdBlock.getTransactions().size() == 1);
        assertTrue("Wrong transaction",
                createdBlock.getTransaction(0).equals(Tx_A_to_B));
    }

    @Test
    public void test15() throws Exception {
        //create a block when there is an invalid transaction processed
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Transaction Tx_A_to_B = new Transaction();
        Tx_A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        Tx_A_to_B.addOutput(-25, kp_B.getPublic());
        Tx_A_to_B.Txsign(kp_A.getPrivate(), 0);
        Tx_A_to_B.finalize();
        blockHandler.processTx(Tx_A_to_B);
        Block createdBlock = blockHandler.createBlock(kp_B.getPublic());

        assertTrue("Not successfully Create a block", createdBlock != null);
        assertTrue("Number of transactions is" + createdBlock.getTransactions().size(),
                createdBlock.getTransactions().size() == 0);
        assertFalse(createdBlock.getTransactions().contains(Tx_A_to_B));
    }

    @Test
    public void test16() throws Exception {
        //create a block with a valid utxo and an invalid utxo that has been claimed in previous block
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block block = new Block(genesisBlock.getHash(), kp_B.getPublic());

        Transaction Tx_A_to_B = new Transaction();
        Tx_A_to_B.addInput(genesisBlock.getCoinbase().getHash(), 0);
        Tx_A_to_B.addOutput(25, kp_B.getPublic());
        Tx_A_to_B.Txsign(kp_A.getPrivate(), 0);
        Tx_A_to_B.finalize();
        block.addTransaction(Tx_A_to_B);

        Transaction Tx_B_to_C = new Transaction();
        Tx_B_to_C.addInput(Tx_A_to_B.getHash(), 0);
        Tx_B_to_C.addOutput(25, kp_C.getPublic());
        Tx_B_to_C.Txsign(kp_B.getPrivate(), 0);
        Tx_B_to_C.finalize();
        block.addTransaction(Tx_B_to_C);

        block.finalize();
        blockHandler.processBlock(block);

        Transaction Tx_B_to_D = new Transaction();
        Tx_B_to_D.addInput(Tx_A_to_B.getHash(), 0);
        Tx_B_to_D.addOutput(25, kp_D.getPublic());
        Tx_B_to_D.Txsign(kp_B.getPrivate(), 0);
        Tx_B_to_D.finalize();
        blockHandler.processTx(Tx_B_to_D);

        Transaction Tx_C_to_D = new Transaction();
        Tx_C_to_D.addInput(Tx_B_to_C.getHash(), 0);
        Tx_C_to_D.addOutput(25, kp_D.getPublic());
        Tx_C_to_D.Txsign(kp_C.getPrivate(), 0);
        Tx_C_to_D.finalize();
        blockHandler.processTx(Tx_C_to_D);

        Block createdBlock = blockHandler.createBlock(kp_B.getPublic());

        assertTrue("Not successfully Create a block", createdBlock != null);
        assertTrue("Number of transactions is" + createdBlock.getTransactions().size(),
                createdBlock.getTransactions().size() == 1);
        assertFalse("block contains TX_B_to_D", createdBlock.getTransactions().contains(Tx_B_to_D));
        assertTrue("block does not contain TX_C_to_D", createdBlock.getTransactions().contains(Tx_C_to_D));

    }

    @Test
    public void test17() throws Exception {
        //create a block chain
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_B.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block createdblock;
        Block prevBlock = genesisBlock;
        Transaction Tx;

        for (int i = 0; i < 10; i++) {
            Tx = new Transaction();
            Tx.addInput(prevBlock.getCoinbase().getHash(), 0);
            Tx.addOutput(25, kp_C.getPublic());
            Tx.Txsign(kp_B.getPrivate(), 0);
            Tx.finalize();
            blockHandler.processTx(Tx);
            createdblock = blockHandler.createBlock(kp_B.getPublic());
            assertTrue("Not successfully Create a block", createdblock != null);
            assertTrue("Wrong parent block", createdblock.getPrevBlockHash() == prevBlock.getHash());
            assertTrue("block does not contain TX", createdblock.getTransactions().contains(Tx));
            prevBlock = createdblock;
        }
    }

    @Test
    public void test18() throws Exception {
        //process multiple blocks at same height, then create a block on top of the oldest block
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block1 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        block1.finalize();
        blockHandler.processBlock(block1);

        Block block2 = new Block(genesisBlock.getHash(), kp_C.getPublic());
        block2.finalize();
        blockHandler.processBlock(block2);

        Block block3 = new Block(genesisBlock.getHash(), kp_D.getPublic());
        block3.finalize();
        blockHandler.processBlock(block3);

        Block createdBlock = blockHandler.createBlock(kp_E.getPublic());
        assertTrue("Not successfully Create a block", createdBlock != null);
        assertTrue("Wrong parent block", createdBlock.getPrevBlockHash() == block1.getHash());
    }

    @Test
    public void test19() throws Exception {
        //create a block, check the block is on the top of highest chain
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block1 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        block1.finalize();
        blockHandler.processBlock(block1);

        Block block2 = new Block(block1.getHash(), kp_C.getPublic());
        block2.finalize();
        blockHandler.processBlock(block2);

        Block block3 = new Block(genesisBlock.getHash(), kp_D.getPublic());
        block3.finalize();
        blockHandler.processBlock(block3);

        Block createdBlock = blockHandler.createBlock(kp_E.getPublic());
        assertTrue("Not successfully Create a block", createdBlock != null);
        assertTrue("Wrong parent block", createdBlock.getPrevBlockHash() == block2.getHash());

    }

    @Test
    public void test20() throws Exception {
        //create a block with a utxo which has been claimed in other chain, the result we expect is true
        //TODO: Test goes here...
        Block genesisBlock = new Block(null, kp_A.getPublic());
        genesisBlock.finalize();
        BlockChain blockChain = new BlockChain(genesisBlock);
        BlockHandler blockHandler = new BlockHandler(blockChain);

        Block block1 = new Block(genesisBlock.getHash(), kp_B.getPublic());
        Transaction TX_A_to_BC = new Transaction();
        TX_A_to_BC.addInput(genesisBlock.getCoinbase().getHash(), 0);
        TX_A_to_BC.addOutput(15, kp_B.getPublic());
        TX_A_to_BC.addOutput(10, kp_C.getPublic());
        TX_A_to_BC.Txsign(kp_A.getPrivate(), 0);
        TX_A_to_BC.finalize();
        block1.addTransaction(TX_A_to_BC);
        block1.finalize();
        blockHandler.processBlock(block1);

        Block block2 = new Block(block1.getHash(), kp_C.getPublic());
        Transaction TX_C_to_D = new Transaction();
        TX_C_to_D.addInput(TX_A_to_BC.getHash(), 1);
        TX_C_to_D.addOutput(10, kp_D.getPublic());
        TX_C_to_D.Txsign(kp_C.getPrivate(), 0);
        TX_C_to_D.finalize();
        block2.addTransaction(TX_C_to_D);
        block2.finalize();
        blockHandler.processBlock(block2);

        Block block3 = new Block(block2.getHash(), kp_C.getPublic());
        Transaction TX_D_to_E = new Transaction();
        TX_D_to_E.addInput(TX_C_to_D.getHash(), 0);
        TX_D_to_E.addOutput(10, kp_E.getPublic());
        TX_D_to_E.Txsign(kp_D.getPrivate(), 0);
        TX_D_to_E.finalize();
        block3.addTransaction(TX_D_to_E);
        block3.finalize();
        blockHandler.processBlock(block3);

        Block block4 = new Block(block1.getHash(), kp_E.getPublic());
        Transaction TX_B_to_E = new Transaction();
        TX_B_to_E.addInput(TX_A_to_BC.getHash(), 0);
        TX_B_to_E.addOutput(15, kp_E.getPublic());
        TX_B_to_E.Txsign(kp_B.getPrivate(), 0);
        TX_B_to_E.finalize();
        block4.addTransaction(TX_B_to_E);
        block4.finalize();
        blockHandler.processBlock(block4);

        Transaction TX_B_to_D = new Transaction();
        TX_B_to_D.addInput(TX_A_to_BC.getHash(), 0);
        TX_B_to_D.addOutput(15, kp_D.getPublic());
        TX_B_to_D.Txsign(kp_B.getPrivate(), 0);
        TX_B_to_D.finalize();
        blockHandler.processTx(TX_B_to_D);
        Block createdBlock = blockHandler.createBlock(kp_D.getPublic());

        assertTrue("Not successfully Create a block", createdBlock != null);
        assertTrue("createdBlock does not contain TX_B_to_D",createdBlock.getTransactions().contains(TX_B_to_D));
    }

}