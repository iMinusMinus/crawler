{ pkgs }: {
    deps = [
        pkgs.graalvm17-ce
        pkgs.maven
        pkgs.chromedriver
        pkgs.chromium
#        pkgs.google-chrome
        pkgs.opencv
        pkgs.replitPackages.jdt-language-server
        pkgs.replitPackages.java-debug
    ];
}