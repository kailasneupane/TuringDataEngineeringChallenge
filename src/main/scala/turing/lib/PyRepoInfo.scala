package turing.lib

/**
  * example:
  *
  * 'repository_url': 'https://github.com/tensorflow/tensorflow',
  * 'number of lines': 59234,
  * 'libraries': ['tensorflow','numpy',..],
  * 'nesting factor': 1.457845,
  * 'code duplication': 23.78955,
  * 'average parameters': 3.456367,
  * 'average variables': 0.03674
  */
case class PyRepoInfo(
                       repository_url: String,
                       number_of_lines: Long,
                       libraries: Array[String],
                       nesting_factor: Double,
                       code_duplication: Double,
                       average_parameters: Double,
                       average_variables: Double
                     )
