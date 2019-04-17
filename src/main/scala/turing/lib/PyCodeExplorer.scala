package turing.lib

import org.apache.spark.SparkContext
import parser.python3.{Python3BaseListener, Python3Parser}

class PyCodeExplorer(sparkContext: SparkContext) extends Python3BaseListener with Serializable {


  //default libs taken from https://docs.python.org/3/py-modindex.html#cap-z
  private var defaultPyImportsList = List("__future__", "__main__", "_dummy_thread", "_thread", "abc", "aifc", "argparse", "array", "ast", "asynchat", "asyncio", "asyncore", "atexit", "audioop", "bdb", "binascii", "binhex", "bisect", "builtins", "bz2", "calendar", "cgi", "cgitb", "chunk", "cmath", "cmd", "code", "codecs", "codeop", "collections", "colorsys", "compileall", "concurrent", "configparser", "contextlib", "contextvars", "copy", "copyreg", "cProfile", "crypt", "csv", "ctypes", "curses", "dataclasses", "datetime", "dbm", "decimal", "difflib", "dis", "distutils", "doctest", "dummy_threading", "email", "encodings", "ensurepip", "enum", "errno", "faulthandler", "fcntl", "filecmp", "fileinput", "fnmatch", "formatter", "fractions", "ftplib", "functools", "gc", "getopt", "getpass", "gettext", "glob", "grp", "gzip", "hashlib", "heapq", "hmac", "html", "http", "imaplib", "imghdr", "imp", "importlib", "inspect", "io", "ipaddress", "itertools", "json", "keyword", "lib2to3", "linecache", "locale", "logging", "lzma", "macpath", "mailbox", "mailcap", "marshal", "math", "mimetypes", "mmap", "modulefinder", "msilib", "msvcrt", "multiprocessing", "netrc", "nis", "nntplib", "numbers", "operator", "optparse", "os", "ossaudiodev", "parser", "pathlib", "pdb", "pickle", "pickletools", "pipes", "pkgutil", "platform", "plistlib", "poplib", "posix", "pprint", "profile", "pstats", "pty", "pwd", "py_compile", "pyclbr", "pydoc", "queue", "quopri", "random", "re", "readline", "reprlib", "resource", "rlcompleter", "runpy", "sched", "secrets", "select", "selectors", "shelve", "shlex", "shutil", "signal", "site", "smtpd", "smtplib", "sndhdr", "socket", "socketserver", "spwd", "sqlite3", "ssl", "stat", "statistics", "string", "stringprep", "struct", "subprocess", "sunau", "symbol", "symtable", "sys", "sysconfig", "syslog", "tabnanny", "tarfile", "telnetlib", "tempfile", "termios", "test", "textwrap", "threading", "time", "timeit", "tkinter", "token", "tokenize", "trace", "traceback", "tracemalloc", "tty", "turtle", "turtledemo", "types", "typing", "unicodedata", "unittest", "urllib", "uu", "uuid", "venv", "warnings", "wave", "weakref", "webbrowser", "winreg", "winsound", "wsgiref", "xdrlib", "xml", "xmlrpc", "zipapp", "zipfile", "zipimport", "zlib")

  private var variableAccumulator = sparkContext.longAccumulator("variables accumulator")
  private var functionAccumulator = sparkContext.longAccumulator("functions accumulator")
  private var functionParamsAccumulator = sparkContext.longAccumulator("functions parameters accumulator")
  private var importsAccumulator = new SetAccumulator[String]()
  sparkContext.register(importsAccumulator, "imports accumulator")

  def getVariableCount = variableAccumulator.value

  def getImportsArray = importsAccumulator.value.toArray.filter(x => !defaultPyImportsList.contains(x))

  def getFunctionsCount = functionAccumulator.value

  def getFunctionParamsCount = functionParamsAccumulator.value

  //variable counter
  override def enterExpr_stmt(ctx: Python3Parser.Expr_stmtContext): Unit = {
    var variableName = ctx.testlist_star_expr().get(0).getText()
    if (!(variableName.trim.endsWith(")") || variableName.trim.startsWith("print") || variableName.trim.startsWith("\""))) {
      variableAccumulator.add(1)
    }
  }

  //import counter1
  override def enterImport_stmt(ctx: Python3Parser.Import_stmtContext): Unit = {
    try {
      var importName = ctx.import_name().dotted_as_names().dotted_as_name().get(0).dotted_name().NAME().get(0).getText().split("\\.")(0)
      importsAccumulator.add(importName)
    } catch {
      case e: NullPointerException => ()
    }
  }

  //import counter2 (may contain internal imports so, we need to filter that)
  override def enterImport_from(ctx: Python3Parser.Import_fromContext): Unit = {
    try {
      var importName = ctx.dotted_name().getText().split("\\.")(0)
      //if (ctx.getText.startsWith("from.")) {
      importsAccumulator.add(importName)
      //}
    } catch {
      case e1: NullPointerException => ()
    }
  }

  //functions counter
  override def enterFuncdef(ctx: Python3Parser.FuncdefContext): Unit = {
    //var functionName = ctx.NAME().getText()
    var functionParamName = ctx.parameters().getText()
    if (!functionParamName.equals("()")) {
      var params = functionParamName.split(",")
      functionParamsAccumulator.add(params.length)
    }
    functionAccumulator.add(1)
  }
}
