// $ANTLR 2.7.4: "unittest.tree.g" -> "UnitTestParser.java"$

    package bluej.parser.ast.gen;
    
    import bluej.parser.SourceSpan;
    import bluej.parser.SourceLocation;
	import bluej.parser.ast.LocatableAST;
	    
    import java.util.*;
    import antlr.BaseAST;

public interface UnitTestParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int BLOCK = 4;
	int MODIFIERS = 5;
	int OBJBLOCK = 6;
	int SLIST = 7;
	int CTOR_DEF = 8;
	int METHOD_DEF = 9;
	int VARIABLE_DEF = 10;
	int INSTANCE_INIT = 11;
	int STATIC_INIT = 12;
	int TYPE = 13;
	int CLASS_DEF = 14;
	int INTERFACE_DEF = 15;
	int PACKAGE_DEF = 16;
	int ARRAY_DECLARATOR = 17;
	int EXTENDS_CLAUSE = 18;
	int IMPLEMENTS_CLAUSE = 19;
	int PARAMETERS = 20;
	int PARAMETER_DEF = 21;
	int LABELED_STAT = 22;
	int TYPECAST = 23;
	int INDEX_OP = 24;
	int POST_INC = 25;
	int POST_DEC = 26;
	int METHOD_CALL = 27;
	int EXPR = 28;
	int ARRAY_INIT = 29;
	int IMPORT = 30;
	int UNARY_MINUS = 31;
	int UNARY_PLUS = 32;
	int CASE_GROUP = 33;
	int ELIST = 34;
	int FOR_INIT = 35;
	int FOR_CONDITION = 36;
	int FOR_ITERATOR = 37;
	int EMPTY_STAT = 38;
	int FINAL = 39;
	int ABSTRACT = 40;
	int STRICTFP = 41;
	int SUPER_CTOR_CALL = 42;
	int CTOR_CALL = 43;
	int VARIABLE_PARAMETER_DEF = 44;
	int STATIC_IMPORT = 45;
	int ENUM_DEF = 46;
	int ENUM_CONSTANT_DEF = 47;
	int FOR = 48;
	int FOR_EACH = 49;
	int ANNOTATION_DEF = 50;
	int ANNOTATION = 51;
	int ANNOTATION_MEMBER_VALUE_PAIR = 52;
	int ANNOTATION_FIELD_DEF = 53;
	int ANNOTATION_ARRAY_INIT = 54;
	int TYPE_ARGUMENT = 55;
	int TYPE_PARAMETERS = 56;
	int WILDCARD_TYPE = 57;
	int TYPE_UPPER_BOUNDS = 58;
	int TYPE_LOWER_BOUNDS = 59;
	int COMMENT_DEF = 60;
	int LITERAL_package = 61;
	int SEMI = 62;
	int LITERAL_import = 63;
	int LITERAL_static = 64;
	int LBRACK = 65;
	int RBRACK = 66;
	int DOT = 67;
	int IDENT = 68;
	int QUESTION = 69;
	int LITERAL_extends = 70;
	int LITERAL_super = 71;
	int LT = 72;
	int COMMA = 73;
	int GT = 74;
	int SR = 75;
	int BSR = 76;
	int LITERAL_void = 77;
	int LITERAL_boolean = 78;
	int LITERAL_byte = 79;
	int LITERAL_char = 80;
	int LITERAL_short = 81;
	int LITERAL_int = 82;
	int LITERAL_float = 83;
	int LITERAL_long = 84;
	int LITERAL_double = 85;
	int STAR = 86;
	int LITERAL_private = 87;
	int LITERAL_public = 88;
	int LITERAL_protected = 89;
	int LITERAL_transient = 90;
	int LITERAL_native = 91;
	int LITERAL_synchronized = 92;
	int LITERAL_volatile = 93;
	int AT = 94;
	int LPAREN = 95;
	int RPAREN = 96;
	int ASSIGN = 97;
	int LCURLY = 98;
	int RCURLY = 99;
	int LITERAL_class = 100;
	int LITERAL_interface = 101;
	int LITERAL_enum = 102;
	int BAND = 103;
	int LITERAL_default = 104;
	int LITERAL_implements = 105;
	int LITERAL_this = 106;
	int LITERAL_throws = 107;
	int TRIPLE_DOT = 108;
	int COLON = 109;
	int LITERAL_if = 110;
	int LITERAL_else = 111;
	int LITERAL_while = 112;
	int LITERAL_do = 113;
	int LITERAL_break = 114;
	int LITERAL_continue = 115;
	int LITERAL_return = 116;
	int LITERAL_switch = 117;
	int LITERAL_throw = 118;
	int LITERAL_assert = 119;
	int LITERAL_for = 120;
	int LITERAL_case = 121;
	int LITERAL_try = 122;
	int LITERAL_finally = 123;
	int LITERAL_catch = 124;
	int PLUS_ASSIGN = 125;
	int MINUS_ASSIGN = 126;
	int STAR_ASSIGN = 127;
	int DIV_ASSIGN = 128;
	int MOD_ASSIGN = 129;
	int SR_ASSIGN = 130;
	int BSR_ASSIGN = 131;
	int SL_ASSIGN = 132;
	int BAND_ASSIGN = 133;
	int BXOR_ASSIGN = 134;
	int BOR_ASSIGN = 135;
	int LOR = 136;
	int LAND = 137;
	int BOR = 138;
	int BXOR = 139;
	int NOT_EQUAL = 140;
	int EQUAL = 141;
	int LE = 142;
	int GE = 143;
	int LITERAL_instanceof = 144;
	int SL = 145;
	int PLUS = 146;
	int MINUS = 147;
	int DIV = 148;
	int MOD = 149;
	int INC = 150;
	int DEC = 151;
	int BNOT = 152;
	int LNOT = 153;
	int LITERAL_true = 154;
	int LITERAL_false = 155;
	int LITERAL_null = 156;
	int LITERAL_new = 157;
	int NUM_INT = 158;
	int CHAR_LITERAL = 159;
	int STRING_LITERAL = 160;
	int NUM_FLOAT = 161;
	int NUM_LONG = 162;
	int NUM_DOUBLE = 163;
	int WS = 164;
	int SL_COMMENT = 165;
	int ML_COMMENT = 166;
	int ESC = 167;
	int HEX_DIGIT = 168;
	int VOCAB = 169;
	int IDENT_LETTER = 170;
	int EXPONENT = 171;
	int FLOAT_SUFFIX = 172;
}
