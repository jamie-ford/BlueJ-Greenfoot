package bluej.testmgr.record;

import bluej.debugger.DebuggerObject;
import bluej.debugger.gentype.GenTypeArray;
import bluej.debugger.gentype.JavaType;
import bluej.debugmgr.objectbench.ObjectBench;
import bluej.debugmgr.objectbench.ObjectWrapper;
import bluej.pkgmgr.PkgMgrFrame;
import bluej.utility.JavaNames;

/**
 * Records a single user interaction with the 
 * method call mechanisms of BlueJ.
 * 
 * This record is for method calls that return a result.
 *
 * @author  Andrew Patterson
 * @version $Id: MethodInvokerRecord.java 5833 2008-08-13 15:48:14Z polle $
 */
public class MethodInvokerRecord extends VoidMethodInvokerRecord
{
    private JavaType returnType;
	private String benchType;
	protected String benchName;
	
	/** How many times has this record been used. */
    private int usageCount;
    
    /** Has the method call been initialised? */
    private boolean methodCallInited = false;
    
    private PkgMgrFrame pkgMgrFrame;
	
    /**
     * Records a method call that returns a result to the user.
     * 
     * @param returnType  the Class of the return type of the method
     * @param command     the method statement to execute
     */
    public MethodInvokerRecord(JavaType returnType, String command, String [] argumentValues, PkgMgrFrame pkgMgrFrame)
    {
    	super(command, argumentValues);
    	
        this.returnType = returnType;
        this.benchType = returnType.toString(false);
        this.benchName = null;
        this.pkgMgrFrame = pkgMgrFrame;
    }

    /**
     * Give this method invoker record a name on the object
     * bench (the user has done a "Get" on the result). The type
     * is the type that the object is on the actual bench.
     * 
     * @param name
     * @param type
     */
	public void setBenchName(String name, String type)
	{
		benchName = name;
		benchType = type;
	}
	
	/**
	 * Construct a declaration for any objects constructed
	 * by this invoker record.
	 * 
	 * @return a String representing the object declaration
	 *         src or null if there is none.
	 */    
    public String toFixtureDeclaration()
    {
		// if it hasn't been assigned a name there is nothing to do for
		// fixture declaration
		if (benchName == null)
			return null;

		// declare the variable		
		StringBuffer sb = new StringBuffer();
		sb.append(fieldDeclarationStart);
		sb.append(benchDeclaration());
		sb.append(benchName);
		sb.append(statementEnd);

		return sb.toString();
    }
    
	/**
	 * Construct a portion of an initialisation method for
	 * this invoker record.
	 *  
	 * @return a String reprenting the object initialisation
	 *         src or null if there is none. 
	 */    
    public String toFixtureSetup()
    {
		if (benchName == null) {
			return secondIndent + command + statementEnd;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(secondIndent);
		sb.append(benchAssignmentTypecast());
        sb.append(statementEnd);
		
		return sb.toString();
    }
    
    /**
     * Construct a portion of a test method for this invoker record.
     * 
     * @return a String representing the test method src
     */
    public String toTestMethod()
    {
        String resultRef = toExpression();

        // with no uses of the result, just invoke the method.
        if (getUsageCount() == 0) {
            return secondIndent + resultRef + statementEnd;
        }

        StringBuffer sb = new StringBuffer();
        // here are all the assertions
        for (int i = 0; i < getAssertionCount(); i++) {
            sb.append(secondIndent);
            sb.append(insertCommandIntoAssertionStatement(getAssertion(i), resultRef));
            sb.append(statementEnd);
        }

        return sb.toString();
    }

    /**
     * Do any initialisation needed for creating the test method. This will set
     * up local variables if the result of the method is used more than once or
     * placed on the bench by using "Get".
     */
    public String toTestMethodInit()
    {
        // If we have already prepared the method call, we return the name that
        // references it.
        if (methodCallInited) {
            return "";
        }

        // Method result has not been put on the bench by using "Get".
        if (benchName == null) {
            if (getUsageCount() > 1) {
                // If the method result is not "Get" onto the bench, and we use the
                // method result more than once, we need to put it on the bench to
                // give it a unique name.
                DebuggerObject result = getResultObject();
                assert (result != null);
                ObjectBench bench = pkgMgrFrame.getObjectBench();
                ObjectWrapper wrapper = ObjectWrapper.getWrapper(pkgMgrFrame, bench, result, result.getGenType(),
                        "result");
                bench.addObject(wrapper); // might change name
                benchName = wrapper.getName();            
            }
            else {
                // Nothing to prepare
                return "";
            }
        }
        else {
            // We used "Get" on the result, so increase usage count.
            incUsageCount();
        }
        
        assert (benchName != null);
        methodCallInited = true;
        // assign result to a local variable with the given benchName.
        return secondIndent + benchDeclaration() + benchAssignmentTypecast() + statementEnd;
    }

    /**
     * This will return a string containing a reference to the method result.
     * Either as the command itself, or the name of a local variable containing
     * the result.
     * 
     * @return Reference to the method result
     */
    @Override
    public String toExpression()
    {
        assert (methodCallInited);

        // Method result has not been put on the bench by using "Get".
        if (benchName == null) {
            return command;
        }
        return benchName;
    }
	
    @Override
    public String getExpressionGlue()
    {
        if(returnType instanceof GenTypeArray) {
            return "";
        } else {
            return ".";
        }
    }

    /**
     * @return A string representing the type name of an object
     */
	private String benchDeclaration()
	{
		return JavaNames.typeName(benchType) + " ";
	}
	
    /**
     * @return A string representing the assignment statement
     *         with an optional typecast to get the type correct
     */
	protected String benchAssignmentTypecast()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(benchName);
		sb.append(" = ");

		// check if a typecast is required
		if (!benchType.equals(returnType.toString(false))) {
			sb.append("(");
			sb.append(benchType);
			sb.append(")");
		}

		sb.append(command);

		return sb.toString();
	}

	@Override
    public void addAssertion(String assertion)
    {
        super.addAssertion(assertion);
        usageCount++;        
    }

    /**
     * Call when using this invoker record as a parent for another invoker
     * record. Increases usage count.
     */
    public void incUsageCount()
    {
        usageCount++;
    }

    private int getUsageCount()
    {
        return usageCount;
    }    
}
