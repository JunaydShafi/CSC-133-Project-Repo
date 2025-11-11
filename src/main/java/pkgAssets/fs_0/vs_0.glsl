#version 430 core

layout (location=0) in vec3 aPos;

uniform mat4 uProjMatrix;
uniform mat4 uViewMatrix;


void main()
{
    gl_Position = uProjMatrix * uViewMatrix * vec4(aPos, 1.0);
}
