modelViewer={
    dead:true,
    destructors:[]
}
function viewModel(shape){
    modelViewer.dead=false;
    var container=document.getElementById("three");
    container.style.width="400px";
    container.style.height="300px";
    
    var scene,camera,renderer,controls,cube;
    function init(){
	destructor=f=>modelViewer.destructors.push(f)
	
	scene=new THREE.Scene();
	destructor(()=>{scene=null})
	
	
	camera=new THREE.PerspectiveCamera(75, 4/3, .01, 1000);
	camera.position.z=10;
	camera.position.y=2;
	destructor(()=>{camera=null});
	
	
	renderer=new THREE.WebGLRenderer({antialias:true});
	renderer.setClearColor("#000000");
	renderer.setSize(400,300);
	container.appendChild(renderer.domElement);
	destructor(()=>renderer.forceContextLoss())
	
	controls=new THREE.OrbitControls(camera, renderer.domElement);
	controls.addEventListener("change", render);
	controls.enableZoom=true;
	destructor(()=>controls.removeEventListener("change", render))

	var geometry=new THREE.Geometry();
	var v=shape.v.map((vert)=>new THREE.Vector3(vert[0],vert[1],vert[2]));
	var f=shape.f.map((face)=>new THREE.Face3(face[0],face[1],face[2]))
	geometry.vertices=v;
	geometry.faces=f;
	destructor(()=>geometry.dispose());

	var material=new THREE.MeshBasicMaterial({color:0x433f81});
	material.side=THREE.DoubleSide
	destructor(()=>material.dispose());
	
	cube=new THREE.Mesh(geometry, material);
	destructor(()=>cube=null);
	
	scene.add(cube);
    }

    function animate(){
	if(!modelViewer.dead)
	    console.log(requestAnimationFrame(animate));
	controls.update();
    }

    function render(){
	renderer.render(scene, camera);
    }

    init();
    render();
    animate();
}
function killViewer(){
    var container=document.getElementById("three");
    container.innerHTML="";
    modelViewer.dead=true;
    modelViewer.destructors.map(destructor=>destructor());
    modelViewer.destructors=[];
}
