
.PHONY: compile test eclipse clean package

all:
	compile

package:
	mvn package

install:
	mvn install

compile:
	mvn compile

test:
	mvn test

# generate .project and .classpath file for Eclipse
eclipse:
	mvn eclipse:eclipse

clean:
	mvn clean
