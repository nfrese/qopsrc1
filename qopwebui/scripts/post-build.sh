#!/bin/bash

set -e

APP_NAME="my-app"
BUILD_DIR="target"
INSTALLER_DIR="$BUILD_DIR/installer"
JAR_FILE=$(find "$BUILD_DIR" -maxdepth 1 -name "*.jar" -not -name "*sources.jar" -not -name "*original*.jar")

echo "[INFO] Creating installer directory..."
rm -rf "$INSTALLER_DIR"
mkdir -p "$INSTALLER_DIR/lib"

echo "[INFO] Copying app JAR..."
cp "$JAR_FILE" "$INSTALLER_DIR/app.jar"

echo "[INFO] Copying dependencies..."
cp "$BUILD_DIR/lib/"*.jar "$INSTALLER_DIR/lib/"

echo "[INFO] Copying Dockerfile..."
cp scripts/Dockerfile "$INSTALLER_DIR/"

echo "[INFO] Creating run script..."
cat <<EOF > "$INSTALLER_DIR/run-docker.sh"
#!/bin/bash
docker build -t $APP_NAME-image .
docker run --rm -p 8080:8080 $APP_NAME-image
EOF
chmod +x "$INSTALLER_DIR/run-docker.sh"

echo "[INFO] Creating local run script..."
cat <<EOF > "$INSTALLER_DIR/run.sh"
#!/bin/bash
# Runs the Spring Boot app locally using the app.jar and lib dependencies

APP_JAR="app.jar"
LIB_DIR="lib"

if [ ! -f "\$APP_JAR" ]; then
  echo "[ERROR] app.jar not found!"
  exit 1
fi

if [ ! -d "\$LIB_DIR" ]; then
  echo "[ERROR] lib directory not found!"
  exit 1
fi

echo "[INFO] Starting application..."
JAVA_HOME=/Users/norbert/apps/amazon-corretto-8.jdk/Contents/Home
JAVA_OPTS="-Xms512m -Xmx2048m -Dspring.profiles.active=prod"
java -cp "\$APP_JAR:\$LIB_DIR/*" at.qop.qopwebui.QopApplication
EOF

chmod +x "$INSTALLER_DIR/run.sh"


echo "[INFO] Zipping installer..."
cd "$BUILD_DIR"
zip -r "${APP_NAME}-installer.zip" installer > /dev/null
cd -

echo "[INFO] Installer ZIP created: $BUILD_DIR/${APP_NAME}-installer.zip"
