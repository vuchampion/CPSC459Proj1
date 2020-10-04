import java.util.*; 
public class TxHandler {

	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) 
	{
		this.utxoPool = new UTXOPool(utxoPool);
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool, 
	 * (2) the signatures on each input of tx are valid, 
	 * (3) no UTXO is claimed multiple times by tx, 
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) 
	{
		UTXOPool uniqueUTXOs = new UTXOPool(); 
		double previousSum = 0; 
		double currentSum = 0; 
		for(int i = 0; i < tx.numInputs(); i++) 
		{
			Transaction.Input input = tx.getInput(i); 
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex); 
			Transaction.Output output = utxoPool.getTxOutput(utxo); 
			if(!utxoPool.contains(utxo))
				return false; 
			if(!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), in.signature))
				return false; 
			if(unqiueUTXOs.contains(utxo)) 
				return false; 
			uniqueUTXOs.addUTXO(utxo, output); 
			previousSum = previousSum + output.value; 
		}
		for(Transaction.Output output : tx.getOutputs())
		{
			if(output.value < 0) 
				return false; 
			currentSum = currentSum + output.value; 
		}
		if(previousSum < currentSum)
			return false; 
		if(previousSum >sss= currentSum)
			return true;
		/*UTXOPool utxo_set = new UTXOPool();
		double previousSum = 0;  	//previous transaction out sum
		double currentSum = 0; 		//current transaction out sum 
		for(int i = 0; i < tx.numInputs(); i++)
		{ 
			Transaction.Input input = tx.getInput(i); 
			UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex); 
			Transaction.Ouput output = utxoPool.getTxOutput(utxo);
			if(!this.utxoPool.contains(utxo))
				return false;
			else if(!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature))
				return false;
			else if(utxo_set.contains(utxo))
				return false; 
			utxo_set.addUTXO(utxo, output); 
			previousSum = previousSum + output.value; 
		}
		for(int j = 0; j < tx.numOutputs(); j++)
		{
			if(output.value < 0) 
				return false; 
			currentSum = currentSum + output.value; 
		}
		if(previousSum < currentSum) 
			return false; 
		return true;*/
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) 
	{
		HashSet<Transaction> valid = new HashSet<>(); 
		for(Transaction tx : possibleTxs)
		{
			if(isValidTx(tx))
			{
				valid.add(tx);
				for(Transaction.Input input : tx.getInputs())
				{
					UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex); 
					utxoPool.removeUTXO(utxo); 
				}
				for(int i = 0; i < tx.numOutputs(); i++)
				{
					Transaction.Output output = tx.getOutput(i); 
					UTXO utxo = new UTXO(tx.getHash(), i); 
					utxoPool.addUTXO(utxo, output); 
				}
			}
		}
		int size = validTxs.size();
		Transaction [] validArray = new Transaction[size];
		return valid.toArray(validArray);
	}
} 
