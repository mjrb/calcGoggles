modelViewer={
    dead:true,
    destructors:[]
}
testshape123={"dx":1,"x":[[-3,0,0],[-2,0,0],[-1,0,0],[0,0,0],[1,0,0],[2,0,0],[3,0,0]],"y":[[-3,-5,0],[-2,0,0],[-1,3,0],[0,4,0],[1,3,0],[2,0,0],[3,-5,0]],"v":[[-3,0,0],[-2,0,0],[-1,0,0],[0,0,0],[1,0,0],[2,0,0],[3,0,0],[-3,-5,0],[-2,0,0],[-1,3,0],[0,4,0],[1,3,0],[2,0,0],[3,-5,0],[-3,0,-6.5],[-2,0,0],[-1,0,3.9000000000000004],[0,0,5.2],[1,0,3.9000000000000004],[2,0,0],[3,0,-6.5],[-3,-5,-6.5],[-2,0,0],[-1,3,3.9000000000000004],[0,4,5.2],[1,3,3.9000000000000004],[2,0,0],[3,-5,-6.5]],"f":[[0,1,7],[8,7,1],[1,2,8],[9,8,2],[2,3,9],[10,9,3],[3,4,10],[11,10,4],[4,5,11],[12,11,5],[5,6,12],[13,12,6],[0,7,14],[21,14,7],[6,13,20],[27,20,13],[0,1,14],[15,14,1],[1,2,15],[16,15,2],[2,3,16],[17,16,3],[3,4,17],[18,17,4],[4,5,18],[19,18,5],[5,6,19],[20,19,6],[7,8,21],[22,21,8],[8,9,22],[23,22,9],[9,10,23],[24,23,10],[10,11,24],[25,24,11],[11,12,25],[26,25,12],[12,13,26],[27,26,13],[14,15,21],[22,21,15],[15,16,22],[23,22,16],[16,17,23],[24,23,17],[17,18,24],[25,24,18],[18,19,25],[26,25,19],[19,20,26],[27,26,20]],"z":[],"w":[],"name":"man"}
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
	destructor(()=>{
	    controls.removeEventListener("change", render);
	    controls.dispose();
	});

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
	    requestAnimationFrame(animate);
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
    console.log("kill");
}
