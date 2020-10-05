import java.util.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
public class TxHandler {
	/* Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is utxoPool. This should make a defensive copy of
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	private UTXOPool utxo_Pool;

	public TxHandler(UTXOPool utxoPool)
	{
		utxo_Pool = new UTXOPool(utxoPool);
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
			Transaction.Output output = utxo_Pool.getTxOutput(utxo);
			if(!utxo_Pool.contains(utxo))
				return false;
			if(!output.address.verifySignature(tx.getRawDataToSign(i), input.signature))
				return false;
			if(uniqueUTXOs.contains(utxo))
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
		return true;
	}

	/* Handles each epoch by receiving an unordered array of proposed
	 * transactions, checking each transaction for correctness,
	 * returning a mutually valid array of accepted transactions,
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs)
	{
		HashSet<Transaction> valid = new HashSet<>();
		for(Transaction transaction : possibleTxs)
		{
			if(isValidTx(transaction))
			{
				valid.add(transaction);
				for(Transaction.Input input : transaction.getInputs())
				{
					UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
					utxo_Pool.removeUTXO(utxo);
				}
				for(int i = 0; i < transaction.numOutputs(); i++)
				{
					Transaction.Output output = transaction.getOutput(i);
					UTXO utxo = new UTXO(transaction.getHash(), i);
					utxo_Pool.addUTXO(utxo, output);
				}
			}
		}
		int size = valid.size();
		Transaction [] validArray = new Transaction[size];
		return valid.toArray(validArray);
	}
}
