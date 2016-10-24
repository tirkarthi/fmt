# Fmt

## Description

A simple text formatter

## Installation

To install lein visit http://leiningen.org/#install

* git clone https://github.com/tirkarthi/fmt
* cd fmt
* lein install

## Usage

    $ lein run Number [File]+

## Options

Number - Number of characters per line for justification
File   - A file to be read. Relative to root.

## Examples

    $ lein run 45 test.txt test1.txt test.txt

### Todos

* Use schema for better proofs
* Tests for the inputs
