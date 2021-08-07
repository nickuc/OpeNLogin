# OpeNLogin

A practical, secure and friendly authentication plugin

### For development:

#### Gradle:
```
repositories {
    maven { 
        url = uri('https://repo.nickuc.com/maven2/') 
    }
}

dependencies {
    compileOnly('com.nickuc.openlogin:openlogin-universal:1.3')
}
```

#### Maven:
```xml
<repositories>
  <repository>
    <id>nickuc-repo</id>
    <url>https://repo.nickuc.com/maven2/</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.nickuc.openlogin</groupId>
    <artifactId>openlogin-universal</artifactId>
    <version>1.3</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

### Usage:
![graph](https://bstats.org/signatures/bukkit/OpeNLogin.svg)
Powered by <a href="https://bstats.org/">bStats</a>