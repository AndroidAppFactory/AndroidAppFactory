package android.content.resource.parser;

import java.util.ArrayList;

public class ReturnValue {
	
	class ValuePair
	{
		public int key = 0;
		public String value = null;
	}
	
	public ValuePair[] result =  null;	
	public ArrayList<Integer> inputs = new ArrayList<Integer>();
	
	public void addQuery(int input)
	{
		inputs.add(input);
	}
	
	public void setQuery()
	{
		if(inputs.size()>0)
		result = new ValuePair[inputs.size()];
		for(int i = 0; i<inputs.size();i++)
		{
			result[i] = new ValuePair();
			result[i].key = inputs.get(i);
		}
	}
	
}
