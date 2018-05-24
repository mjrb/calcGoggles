var container=$("#three")[0];
container.style.width="400px";
container.style.height="300px";

var scene,camera,renderer,controls,cube;
function init(){
    scene=new THREE.Scene();

    camera=new THREE.PerspectiveCamera(75, 4/3, .01, 1000);
    camera.position.z=10;
    camera.position.y=2;
    
    renderer=new THREE.WebGLRenderer({antialias:true});
    renderer.setClearColor("#000000");
    renderer.setSize(400,300);
    container.appendChild(renderer.domElement);
    
    controls=new THREE.OrbitControls(camera, renderer.domElement);
    controls.addEventListener("change", render);
    controls.enableZoom=true;

    var geometry=new THREE.Geometry();
    var v=shape.v.map((vert)=>new THREE.Vector3(vert[0],vert[1],vert[2]));
    var f=shape.f.map((face)=>new THREE.Face3(face[0],face[1],face[2]))
    geometry.vertices=v;
    geometry.faces=f;

    var material=new THREE.MeshBasicMaterial({color:0x433f81});
    material.side=THREE.DoubleSide
    cube=new THREE.Mesh(geometry, material);
    
    scene.add(cube);
}

function animate(){
    requestAnimationFrame(animate);
    controls.update();
}

function render(){
    renderer.render(scene, camera);
}

init();
render();
animate();

