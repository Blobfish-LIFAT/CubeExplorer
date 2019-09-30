// Generated from /home/alex/IdeaProjects/CubeExplorer/src/main/antlr4/MDXExp.g4 by ANTLR 4.7.2
package com.olap3.cubeexplorer.mdxparser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MDXExpLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, Validtext=2, Atom=3, TEXT=4, OP=5, TIRET=6, LPAREN=7, RPAREN=8, 
		WS=9;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "Validtext", "Atom", "FRENCH", "LOWERCASE", "UPPERCASE", "TEXT", 
			"OP", "TIRET", "LPAREN", "RPAREN", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.'", null, null, null, null, null, "'('", "')'", "' '"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, "Validtext", "Atom", "TEXT", "OP", "TIRET", "LPAREN", "RPAREN", 
			"WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public MDXExpLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "MDXExp.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\13I\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\3\2\3\2\3\3\6\3\37\n\3\r\3\16\3 \3\3\3\3\3\3\3\3\3"+
		"\3\7\3(\n\3\f\3\16\3+\13\3\3\4\3\4\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b"+
		"\3\b\3\b\6\b:\n\b\r\b\16\b;\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r"+
		"\3\r\3\r\2\2\16\3\3\5\4\7\5\t\2\13\2\r\2\17\6\21\7\23\b\25\t\27\n\31\13"+
		"\3\2\7\b\2))\u00e2\u00e2\u00e4\u00e4\u00ea\u00ec\u00f6\u00f6\u00fd\u00fd"+
		"\3\2c|\3\2C\\\5\2,-//\61\61\4\2//aa\2N\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\3\33\3\2\2\2\5\36\3\2\2\2\7,\3\2\2\2\t\60\3\2\2\2\13\62"+
		"\3\2\2\2\r\64\3\2\2\2\179\3\2\2\2\21=\3\2\2\2\23?\3\2\2\2\25A\3\2\2\2"+
		"\27C\3\2\2\2\31E\3\2\2\2\33\34\7\60\2\2\34\4\3\2\2\2\35\37\5\17\b\2\36"+
		"\35\3\2\2\2\37 \3\2\2\2 \36\3\2\2\2 !\3\2\2\2!)\3\2\2\2\"(\5\17\b\2#("+
		"\5\23\n\2$(\5\31\r\2%(\5\25\13\2&(\5\27\f\2\'\"\3\2\2\2\'#\3\2\2\2\'$"+
		"\3\2\2\2\'%\3\2\2\2\'&\3\2\2\2(+\3\2\2\2)\'\3\2\2\2)*\3\2\2\2*\6\3\2\2"+
		"\2+)\3\2\2\2,-\7]\2\2-.\5\5\3\2./\7_\2\2/\b\3\2\2\2\60\61\t\2\2\2\61\n"+
		"\3\2\2\2\62\63\t\3\2\2\63\f\3\2\2\2\64\65\t\4\2\2\65\16\3\2\2\2\66:\5"+
		"\13\6\2\67:\5\r\7\28:\5\t\5\29\66\3\2\2\29\67\3\2\2\298\3\2\2\2:;\3\2"+
		"\2\2;9\3\2\2\2;<\3\2\2\2<\20\3\2\2\2=>\t\5\2\2>\22\3\2\2\2?@\t\6\2\2@"+
		"\24\3\2\2\2AB\7*\2\2B\26\3\2\2\2CD\7+\2\2D\30\3\2\2\2EF\7\"\2\2FG\3\2"+
		"\2\2GH\b\r\2\2H\32\3\2\2\2\b\2 \')9;\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}