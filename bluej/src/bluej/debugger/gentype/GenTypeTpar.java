package bluej.debugger.gentype;

import java.util.Map;


public class GenTypeTpar extends GenTypeSolid
{
    private String name;
    
    public GenTypeTpar(String parname)
    {
        name = parname;
    }
    
    public String getTparName()
    {
        return name;
    }
    
    public String toString(boolean stripPrefix)
    {
        return name;
    }
    
    public boolean equals(GenTypeParameterizable other)
    {
        if( ! (other instanceof GenTypeTpar) )
            return false;
        return name.equals(((GenTypeTpar)other).name);
    }
    
    protected GenTypeParameterizable mapTparsToTypes(Map tparams)
    {
        GenTypeParameterizable newType = (GenTypeParameterizable)tparams.get(name);
        if( newType == null )
            return this;
        else
            return newType;
    }
    
    protected void getParamsFromTemplate(Map map, GenTypeParameterizable template)
    {
        // TODO - may need to "precisify".
        map.put(name, template);
    }
    
    protected GenTypeParameterizable precisify(GenTypeParameterizable other)
    {
        return other;
    }

}
