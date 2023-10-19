package Util;

import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
import sootup.core.types.VoidType;

public class DexUtil {

    public static Type toSootType(String typeDescriptor, int pos){
        Type type = null;
        char typeDesignator = typeDescriptor.charAt(pos);
        switch(typeDesignator){
            case 'Z': // boolean
                type = PrimitiveType.BooleanType.getInstance();
                break;
            case 'B': // byte
                type = PrimitiveType.ByteType.getInstance();
                break;
            case 'S': // short
                type = PrimitiveType.ShortType.getInstance();
                break;
            case 'C': // char
                type = PrimitiveType.CharType.getInstance();
                break;
            case 'I': // int
                type = PrimitiveType.IntType.getInstance();
                break;
            case 'J': // long
                type = PrimitiveType.LongType.getInstance();
                break;
            case 'F': // float
                type = PrimitiveType.FloatType.getInstance();
                break;
            case 'D': // double
                type = PrimitiveType.DoubleType.getInstance();
                break;
            case 'L': // object
                if(Util.isByteCodeClassName(typeDescriptor)){
                    typeDescriptor = Util.dottedClassName(typeDescriptor);
                }
                type = Util.getClassTypeFromClassName(typeDescriptor);
                break;
            case 'V': // void
                type = VoidType.getInstance();
                break;
            case '[': // array
                Type sootType = toSootType(typeDescriptor, pos + 1);
                if(sootType != null) {
                    type = Type.createArrayType(sootType, 1);
                }
                break;
            default:
                type = UnknownType.getInstance();
        }
        return type;
    }

}
