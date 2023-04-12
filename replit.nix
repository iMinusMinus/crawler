{ pkgs }: {
    deps = [
        pkgs.graalvm17-ce
        pkgs.maven
        pkgs.chromium
        pkgs.chromedriver
        pkgs.replitPackages.jdt-language-server
        pkgs.replitPackages.java-debug
    ];
}