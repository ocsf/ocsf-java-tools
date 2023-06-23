plugins {
    id("project.java-conventions")
    `java-library`
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    api(project(":utils"))
}
