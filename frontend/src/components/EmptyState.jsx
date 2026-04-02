export default function EmptyState({ title = 'No data found', subtitle, action }) {
  return (
    <div className="text-center py-16">
      <div className="text-6xl mb-4">📭</div>
      <h3 className="text-lg font-semibold text-gray-700 mb-2">{title}</h3>
      {subtitle && <p className="text-gray-400 text-sm mb-4">{subtitle}</p>}
      {action && action}
    </div>
  );
}
