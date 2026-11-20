# Reproduction Recipe

Step 3 of the [`github-issues`](../SKILL.md) skill. It runs a minimal snippet against the compiled library, so the "Actual output" in an issue comes from a real run. Run every command from the repository root, ideally on an up-to-date `master`.

## 1. Build and resolve the classpath

```bash
./mvnw -B -q compile
./mvnw -B -q dependency:build-classpath -Dmdep.outputFile=<scratch>/cp.txt
```

`<scratch>` is your scratchpad directory, or `target/` if you don't have one. `compile` fills `target/classes`; use `./mvnw -B -q package -DskipTests` instead if the bug depends on packaged resources.

## 2. Write the snippet

Write `<scratch>/Repro.java`. Use only the public API (`Masker` factory methods and builders), synthetic data, and print the result or the exception:

```java
import io.github.ehayik.jmaskify.Masker;

public class Repro {
    public static void main(String[] args) {
        var masker = Masker.creditCard();
        System.out.println(masker.apply("4111-1111-1111-1111"));
    }
}
```

Keep it as small as possible: the same snippet goes into "Steps to reproduce". Never put it under `src/`.

## 3. Compile and run

```bash
CP="target/classes:$(cat <scratch>/cp.txt)"
javac -d <scratch>/out -cp "$CP" <scratch>/Repro.java
java -cp "<scratch>/out:$CP" Repro
```

On Windows, use `;` instead of `:` as the classpath separator.

## 4. Capture the result

- Copy stdout, stderr, and any stack trace into "Actual output" verbatim. You may trim a long stack trace below the first JMaskify frame, and say so.
- If the run doesn't show the bug, don't file. Report that it didn't reproduce.
- Collect the environment details:

  ```bash
  ./mvnw -q help:evaluate -Dexpression=project.version -DforceStdout   # JMaskify version
  git rev-parse --short HEAD                                             # commit SHA
  java -version                                                          # Java version
  ```

  Add a dependency version (for example Jackson for `JsonMasker`) only when it matters, taken from `./mvnw -q dependency:list` or `pom.xml`.
