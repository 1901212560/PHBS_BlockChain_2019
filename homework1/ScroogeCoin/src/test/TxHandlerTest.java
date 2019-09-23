import java.security.*;

import org.junit.Test;
import org.junit.Before; 
import org.junit.After;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;


/** 
* TxHandler Tester. 
* 
* @author <BOLAN>
* @since <pre>9ÔÂ 22, 2019</pre> 
* @version 1.0 
*/ 
public class TxHandlerTest {

    byte[] origin_tx_hashA;
    byte[] origin_tx_hashB;
    byte[] origin_tx_hashC;
    byte[] origin_tx_hashD;
    byte[] origin_tx_hashE;

    TxHandler TxhandlerTest;
    UTXOPool pool;

    KeyPair kp_A;
    KeyPair kp_B;
    KeyPair kp_C;
    KeyPair kp_D;
    KeyPair kp_E;
    PublicKey PKA,PKB,PKC,PKD,PKE;
    PrivateKey SKA,SKB,SKC,SKD,SKE;



    @Before   //Generate all initial data
    public void before() throws Exception {

        //Generate key pairs, for A B C D E
        KeyPair kp_A = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        SKA=kp_A.getPrivate();
        PKA=kp_A.getPublic();

        KeyPair kp_B = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        SKB=kp_B.getPrivate();
        PKB=kp_B.getPublic();

        KeyPair kp_C = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        SKC=kp_C.getPrivate();
        PKC=kp_C.getPublic();

        KeyPair kp_D = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        SKD=kp_D.getPrivate();
        PKD=kp_D.getPublic();

        KeyPair kp_E = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        SKE=kp_E.getPrivate();
        PKE=kp_E.getPublic();

        // initial coin for A B
        Transaction.Output outA = new Transaction().new Output(10, PKA);
        Transaction.Output outB = new Transaction().new Output(5, PKB);

        origin_tx_hashA = new String("origin_tx_hashA").getBytes();
        origin_tx_hashB = new String("origin_tx_hashB").getBytes();
        origin_tx_hashC  = new String("origin_tx_hashC").getBytes();

        pool = new UTXOPool();
        pool.addUTXO(new UTXO(origin_tx_hashA, 0), outA);
        pool.addUTXO(new UTXO(origin_tx_hashB, 0), outB);

        TxhandlerTest = new TxHandler(pool);
    }

    @After
    public void after() throws Exception {
    }

    /*
    valid transaction test
     */
    @Test
    public void test1valid() throws Exception {
        //test1: A to B 2 coins

        Transaction tx_A_to_B = new Transaction();
        tx_A_to_B.addInput(origin_tx_hashA,0);
        tx_A_to_B.addOutput(2,PKB);
        tx_A_to_B.Txsign(SKA,0);
        tx_A_to_B.finalize();

        Transaction[] trans = {tx_A_to_B};
        Transaction[] result = {tx_A_to_B};
        assertThat(TxhandlerTest.handleTxs(trans), is(result));
   }
    @Test
    public void test2valid() throws Exception {
        //test2:
        // A to B 2 coins
        //B to C 5coins  B to D 1coin
        //C to E 2coins  D to E 1coin

        Transaction tx_A_to_B = new Transaction();
        tx_A_to_B.addInput(origin_tx_hashA,0);
        tx_A_to_B.addOutput(2,PKB);
        tx_A_to_B.Txsign(SKA,0);
        tx_A_to_B.finalize();

        Transaction tx_B_to_CD = new Transaction();
        tx_B_to_CD.addInput(origin_tx_hashB,0);
        tx_B_to_CD.addInput(tx_A_to_B.getHash(),0);
        tx_B_to_CD.addOutput(5,PKC);
        tx_B_to_CD.addOutput(1,PKD);
        tx_B_to_CD.Txsign(SKB,0);
        tx_B_to_CD.Txsign(SKB,1);
        tx_B_to_CD.finalize();

        Transaction tx_CD_to_E = new Transaction();
        tx_CD_to_E.addInput(tx_B_to_CD.getHash(),0);
        tx_CD_to_E.addInput(tx_B_to_CD.getHash(),1);
        tx_CD_to_E.addOutput(2,PKE);
        tx_CD_to_E.addOutput(1,PKE);
        tx_CD_to_E.Txsign(SKC,0);
        tx_CD_to_E.Txsign(SKD,1);
        tx_CD_to_E.finalize();

        Transaction[] trans = {tx_A_to_B,tx_B_to_CD,tx_CD_to_E};
        Transaction[] result = {tx_A_to_B,tx_B_to_CD,tx_CD_to_E};
        assertTrue( TxhandlerTest.handleTxs(trans).length == 3);
        //assertThat(TxhandlerTest.handleTxs(trans), is(result));  //influenced by tx's order

        /*
        for (int i=0;i<3;i++){
            System.out.println("TxhandlerTest.isValidTx(tx"+(i+1)+") returns: " + TxhandlerTest.isValidTx(trans[i]));
                    }
        System.out.println("TxhandlerTest.handleTxs(trans) returns: " +
                TxhandlerTest.handleTxs(trans).length + " transaction(s)");
        */
    }

    /*
    invalid transaction test
     */
    @Test
    public void test1invalid() throws Exception {
        //test1: output claimed is not in the current UTXO pool

        Transaction tx_C_to_D = new Transaction();
        tx_C_to_D.addInput(origin_tx_hashC,0);
        tx_C_to_D.addOutput(2,PKD);
        tx_C_to_D.Txsign(SKC,0);
        tx_C_to_D.finalize();

        Transaction[] trans = {tx_C_to_D};
        Transaction[] result = {};
        //assertTrue( TxhandlerTest.handleTxs(trans).length == 0);
        assertThat(TxhandlerTest.handleTxs(trans), is(result));
    }

    @Test
    public void test2invalid() throws Exception {
        //test2: wrong signature, e.g. C try to use its private key to spend A's coin

        Transaction tx_A_to_B = new Transaction();
        tx_A_to_B .addInput(origin_tx_hashA,0);
        tx_A_to_B .addOutput(2,PKB);
        tx_A_to_B .Txsign(SKC,0);
        tx_A_to_B .finalize();

        Transaction[] trans = {tx_A_to_B };
        Transaction[] result = {};
        //assertTrue( TxhandlerTest.handleTxs(trans).length == 0);
        assertThat(TxhandlerTest.handleTxs(trans), is(result));
    }

    @Test
    public void test3invalid() throws Exception {
        //test3: a negative output

        Transaction tx_A_to_B = new Transaction();
        tx_A_to_B .addInput(origin_tx_hashA,0);
        tx_A_to_B .addOutput(-2,PKB);
        tx_A_to_B .Txsign(SKA,0);
        tx_A_to_B .finalize();

        Transaction[] trans = {tx_A_to_B };
        Transaction[] result = {};
        //assertTrue( TxhandlerTest.handleTxs(trans).length == 0);
        assertThat(TxhandlerTest.handleTxs(trans), is(result));
    }

    @Test
    public void test4invalid() throws Exception {
        //test4: no sufficient coins

        Transaction tx_A_to_B = new Transaction();
        tx_A_to_B .addInput(origin_tx_hashA,0);
        tx_A_to_B .addOutput(20,PKB);
        tx_A_to_B .Txsign(SKA,0);
        tx_A_to_B .finalize();

        Transaction[] trans = {tx_A_to_B};
        Transaction[] result = {};
        //assertTrue( TxhandlerTest.handleTxs(trans).length == 0);
        assertThat(TxhandlerTest.handleTxs(trans), is(result));
    }

    @Test
    public void test5invalid() throws Exception {
        //test5: double spending
        //A to B 10coins
        //B to C 10coins
        //C to D 10coins
        //then A to E 10coins, which is invalid ,because A'coins have been sent to B

        Transaction tx_A_to_B = new Transaction();  //tx1
        tx_A_to_B .addInput(origin_tx_hashA,0);
        tx_A_to_B .addOutput(10,PKB);
        tx_A_to_B .Txsign(SKA,0);
        tx_A_to_B .finalize();

        Transaction tx_B_to_C = new Transaction();   //tx2
        tx_B_to_C .addInput(tx_A_to_B.getHash(),0);
        tx_B_to_C .addOutput(10,PKC);
        tx_B_to_C .Txsign(SKB,0);
        tx_B_to_C .finalize();

        Transaction tx_C_to_D = new Transaction();   //tx3
        tx_C_to_D .addInput(tx_B_to_C.getHash(),0);
        tx_C_to_D .addOutput(10,PKD);
        tx_C_to_D .Txsign(SKC,0);
        tx_C_to_D .finalize();

        Transaction tx_A_to_E = new Transaction();   //tx4
        tx_A_to_E .addInput(origin_tx_hashA,0);
        tx_A_to_E .addOutput(10,PKE);
        tx_A_to_E .Txsign(SKA,0);
        tx_A_to_E .finalize();

        Transaction[] trans = {tx_A_to_B,tx_B_to_C,tx_C_to_D,tx_A_to_E};
        Transaction[] result = {tx_A_to_B,tx_B_to_C,tx_C_to_D};
        assertTrue( TxhandlerTest.handleTxs(trans).length == 3);
        //assertThat(TxhandlerTest.handleTxs(trans), is(result));
        /*
        for (int i=0;i<4;i++){
            System.out.println("TxhandlerTest.isValidTx(tx"+(i+1)+") returns: " + TxhandlerTest.isValidTx(trans[i]));
                    }
        System.out.println("TxhandlerTest.handleTxs(trans) returns: " +
                TxhandlerTest.handleTxs(trans).length + " transaction(s)");
         */

    }

}


