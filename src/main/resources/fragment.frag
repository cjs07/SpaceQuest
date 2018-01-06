#version 330 core

in vec2 UV;
in float selected;

out vec3 color;

uniform sampler2D textureSampler;

void main() {
    color = texture(textureSampler, UV).rgb;
    if (selected > 0) {
        color = vec3(1, 1, 1);
    }
}