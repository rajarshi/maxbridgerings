# Maximally Bridging Rings

This repository contains code to compute one or more maximally bridging rings (MBR) as described in
[Marth et al, Nature, 2015](http://dx.doi.org/10.1038/nature16440). These are used as part of a retrosynthetic
strategy. The original work described
the method and provide an online tool to run the computation on a submitted SDF/MOL file. However
no source code was provided. Hence this tool.

The code here can be used to identify the MBR(s) in an input molecule. Currently the output is an SVG
depiction of the molecule with the first MBR highlighted in red. The code allows you to access the subset
of bridgehead atoms that are maximally bridging.

_Note this was a quick hack and hasn't been extensively tested
but seems to work on the first few examples in the SI provided by the authors._

## Build

The code depends on [CDK](https://github.com/cdk/cdk) (1.5.12) and a self-containd JAR file can be built
using
```
mvn clean package
```

## Usage

The tool can then be run by specifying a molecule in SMILES format and the molecule title.
```
java -jar target/MaximallyBridgingRings-1.0-jar-with-dependencies.jar "CC(C)C1CCC2(C3C1C2CC=C3C)C" Copaene
```
The depiction will be in a file called `Copaene.svg`.