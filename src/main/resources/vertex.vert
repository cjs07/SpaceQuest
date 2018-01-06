#version 330 core

// Input vertex data, different for all executions of this shader.
layout(location = 0) in vec3 vertexPosition_modelspace;
layout(location = 1) in vec2 vertexUV;
layout(location = 2) in float selected;

out vec2 UV;
out float outSelected;

uniform mat4 MVP;

void main() {
    gl_Position = MVP * vec4(vertexPosition_modelspace, 1);
    UV = vertexUV;
    if (selected > 0) {
        outSelected = 1;
    } else {
        outSelected = 0;
    }
}

