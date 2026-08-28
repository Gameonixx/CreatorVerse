import CreatorContentList from '../components/creator/CreatorContentList';

export default function MyContentPage() {
  return (
    <div className="page-container">
      <div className="page-header">
        <h1 className="page-title">My Content</h1>
      </div>
      <CreatorContentList />
    </div>
  );
}
