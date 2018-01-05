#version 330 core

layout(location = 0) in vec2 vertexPosition_screenspace;

uniform vec2 screen;

void main() {
    vec2 vertexPosition = vertexPosition_screenspace - vec2(512, 384);
    vertexPosition /= vec2(512, 384);
	gl_Position = vec4(vertexPosition, 0.0, 1.0);
}
